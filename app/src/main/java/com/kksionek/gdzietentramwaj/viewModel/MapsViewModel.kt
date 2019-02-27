package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.support.v7.util.DiffUtil
import android.util.Log
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.JsonSyntaxException
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.repository.LocationRepository
import com.kksionek.gdzietentramwaj.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.repository.TramRepository
import com.kksionek.gdzietentramwaj.view.MapControls
import com.kksionek.gdzietentramwaj.view.TramMarker
import com.kksionek.gdzietentramwaj.view.UiState
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.properties.Delegates

private const val MAX_VISIBLE_MARKERS = 50

class MapsViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository,
    private val crashReportingService: CrashReportingService
) : ViewModel() {

    object NoTramsLoadedException : Throwable()

    val favoriteView = MutableLiveData<Boolean>().apply {
        value = mapsViewSettingsRepository.isFavoriteTramViewEnabled()
    }

    private val _mapControls = MutableLiveData<MapControls>()
    val mapControls: LiveData<MapControls> = _mapControls

    private var allKnownTrams = mapOf<String, TramMarker>()
    private var allTrams: List<TramMarker> = emptyList()

    private val _tramData = MutableLiveData<UiState>()
    val tramData: LiveData<UiState> = _tramData

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location> = _lastLocation

    var visibleRegion by Delegates.observable<LatLngBounds?>(null) { _, _, _ ->
        showOrZoom(false)
    }

    private val compositeDisposable = CompositeDisposable()
    private var tramFetchingDisposable: Disposable? = null

    private val favoriteLock = ReentrantReadWriteLock()
    private var favoriteTrams = emptyList<String>()

    init {
        observeFavoriteTrams()
    }

    private fun observeFavoriteTrams() {
        compositeDisposable.add(tramRepository.favoriteTrams
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { throwable: Throwable ->
                Log.e(TAG, "Failed getting all the favorites from the database", throwable)
                crashReportingService.reportCrash(
                    throwable,
                    "Failed getting the favorite data from database"
                )
                Flowable.empty<List<String>>()
            }
            .subscribe {
                favoriteLock.write {
                    favoriteTrams = it ?: emptyList()
                }
            }
        )
    }

    fun forceReloadLastLocation() {
        compositeDisposable.add(locationRepository.lastKnownLocation
            .subscribe { location ->
                _lastLocation.postValue(location)
            })
    }

    private fun startFetchingTrams() {
        tramFetchingDisposable?.dispose()
        tramFetchingDisposable = tramRepository.dataStream
            .subscribeOn(Schedulers.io())
            .map {
                if (it is NetworkOperationResult.Success<List<TramData>> && it.tramDataHashMap.isEmpty()) {
                    NetworkOperationResult.Error(NoTramsLoadedException)
                } else {
                    it
                }
            }
            .subscribe { operationResult ->
                if (operationResult is NetworkOperationResult.Success<List<TramData>>) {
                    allTrams = operationResult.tramDataHashMap
                        .map {
                            allKnownTrams[it.id]?.apply { updatePosition(it.latLng) } ?: TramMarker(
                                it
                            )
                        }
                    allKnownTrams = allTrams.map { it.id to it }.toMap()

                    showOrZoom(true, true)
                } else if (operationResult is NetworkOperationResult.Error) {
                    val uiState: UiState.Error = when (operationResult.throwable) {
                        NoTramsLoadedException -> UiState.Error(R.string.none_position_is_up_to_date)
                        is UnknownHostException, is SocketTimeoutException -> UiState.Error(R.string.error_internet)
                        else -> {
                            if (!BuildConfig.DEBUG
                                && operationResult.throwable !is JsonSyntaxException
                                && operationResult.throwable !is IllegalStateException
                                && operationResult.throwable !is HttpException
                            ) {
                                crashReportingService.reportCrash(
                                    operationResult.throwable,
                                    "Error handled with a toast."
                                )
                            }
                            if (BuildConfig.DEBUG) {
                                UiState.Error(
                                    R.string.debug_error_message,
                                    listOf(
                                        operationResult.throwable.javaClass.simpleName,
                                        operationResult.throwable.message
                                            ?: "null"
                                    )
                                )
                            } else {
                                UiState.Error(R.string.error_ztm)
                            }
                        }
                    }
                    _tramData.postValue(uiState)
                }
            }
    }

    class DiffCallback(
        private val oldList: List<TramMarker>,
        private val newList: List<TramMarker>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(p0: Int, p1: Int): Boolean = oldList[p0].id == newList[p1].id

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(p0: Int, p1: Int): Boolean =
            oldList[p0].finalPosition == newList[p1].finalPosition
                    && oldList[p0].prevPosition == newList[p1].prevPosition
    }

    private fun showOrZoom(animate: Boolean, newData: Boolean = false) {
        val onlyVisibleTrams = allTrams
            .filter { isMarkerLineVisible(it.tramLine) }
            .filter { visibleRegion?.let { region -> it.isOnMap(region) } ?: false }

        if (onlyVisibleTrams.size <= MAX_VISIBLE_MARKERS) {
            postMarkers(onlyVisibleTrams, animate, newData)
        } else {
            _mapControls.postValue(MapControls.ZoomIn)
        }
    }

    private fun postMarkers(
        visibleTrams: List<TramMarker>,
        animate: Boolean,
        newData: Boolean = false
    ) {
        _tramData.postValue(UiState.Success(visibleTrams, animate, newData))
    }

    private fun isMarkerLineVisible(line: String): Boolean {
        return favoriteLock.read {
            !(favoriteView.value ?: false) || line in favoriteTrams
        }
    }

    private fun stopFetchingTrams() {
        tramFetchingDisposable?.dispose()
    }

    fun toggleFavorite() {
        val favoriteViewOn = !(favoriteView.value!!)
        mapsViewSettingsRepository.saveFavoriteTramViewState(favoriteViewOn)
        favoriteView.value = favoriteViewOn
        showOrZoom(false)
    }

    fun forceReload() {
        tramRepository.forceReload()
    }

    fun onResume() {
        startFetchingTrams()
    }

    fun onPause() {
        stopFetchingTrams()
    }

    override fun onCleared() {
        stopFetchingTrams()
        compositeDisposable.clear()
        super.onCleared()
    }

    companion object {
        const val TAG = "MapsViewModel"
    }
}
