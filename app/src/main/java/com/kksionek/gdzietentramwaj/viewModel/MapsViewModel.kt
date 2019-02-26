package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.support.v7.util.DiffUtil
import com.crashlytics.android.Crashlytics
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
import com.google.gson.JsonSyntaxException
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.repository.LocationRepository
import com.kksionek.gdzietentramwaj.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.repository.TramRepository
import com.kksionek.gdzietentramwaj.view.MapControls
import com.kksionek.gdzietentramwaj.view.TramMarker
import com.kksionek.gdzietentramwaj.view.UiState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.properties.Delegates

private const val MAX_VISIBLE_MARKERS = 50

class MapsViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository
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

    var visibleRegion by Delegates.observable<LatLngBounds?>(null) { _, _, _ ->
        showOrZoom(false)
    }

    private val compositeDisposable = CompositeDisposable()

    private fun startFetchingTrams() {
        compositeDisposable.add(tramRepository.dataStream
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
                                // TODO Move this to wrapper class
                                Crashlytics.log("Error handled with a toast.")
                                Crashlytics.logException(operationResult.throwable)
                            }
                            if (BuildConfig.DEBUG) {
                                UiState.Error(
                                    R.string.debug_error_message,
                                    listOf(
                                        operationResult.throwable.javaClass.simpleName,
                                        operationResult.throwable.message
                                            ?: "null" //TODO Check why an exception is thrown here
//                                        Process: com.kksionek.gdzietentramwaj, PID: 11036
//                                java.util.MissingFormatArgumentException: Format specifier '%2$s'
//                                at java.util.Formatter.format(Formatter.java:2528)
//                                at java.util.Formatter.format(Formatter.java:2458)
//                                at java.lang.String.format(String.java:2814)
//                                at android.content.res.Resources.getString(Resources.java:472)
//                                at android.content.Context.getString(Context.java:572)
//                                at com.kksionek.gdzietentramwaj.view.MapsActivity$tramDataObserver$1.onChanged(MapsActivity.kt:117)
//                                at com.kksionek.gdzietentramwaj.view.MapsActivity$tramDataObserver$1.onChanged(MapsActivity.kt:61)
//                                at android.arch.lifecycle.LiveData.considerNotify(LiveData.java:109)
//                                at android.arch.lifecycle.LiveData.dispatchingValue(LiveData.java:126)
//                                at android.arch.lifecycle.LiveData.setValue(LiveData.java:282)
//                                at android.arch.lifecycle.MutableLiveData.setValue(MutableLiveData.java:33)
//                                at android.arch.lifecycle.LiveData$1.run(LiveData.java:87)
//                                at android.os.Handler.handleCallback(Handler.java:789)
//                                at android.os.Handler.dispatchMessage(Handler.java:98)
//                                at android.os.Looper.loop(Looper.java:164)
//                                at android.app.ActivityThread.main(ActivityThread.java:6944)
//                                at java.lang.reflect.Method.invoke(Native Method)
//                                at com.android.internal.os.Zygote$MethodAndArgsCaller.run(Zygote.java:327)
//                                at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:1374)
                                    )
                                )
                            } else {
                                UiState.Error(R.string.error_ztm)
                            }
                        }
                    }
                    _tramData.postValue(uiState)
                }
            })
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

    private fun isMarkerLineVisible(line: String) =
        !(favoriteView.value
            ?: false) || line in tramRepository.favoriteTrams.value ?: emptyList() // TODO: Getting the value from the database is not efficient

    private fun stopFetchingTrams() {
        compositeDisposable.clear()
    }

    fun getLastKnownLocation(): Task<Location> =
        locationRepository.lastKnownLocation

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
        super.onCleared()
    }
}
