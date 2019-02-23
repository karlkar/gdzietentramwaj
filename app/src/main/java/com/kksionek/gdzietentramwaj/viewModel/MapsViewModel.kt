package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
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

//    enum class TramAction {
//        ADD,
//        REMOVE,
//        UPDATE
//    }

    val favoriteView = MutableLiveData<Boolean>().apply {
        value = mapsViewSettingsRepository.isFavoriteTramViewEnabled()
    }

    private val _mapControls = MutableLiveData<MapControls>()
    val mapControls: LiveData<MapControls> = _mapControls

    private val allKnownTrams = mutableMapOf<String, TramMarker>()
//    private val lastTramUpdate = mutableMapOf<TramAction, MutableMap<String, TramMarker>>()

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
//                    lastTramUpdate.clear()
//                    val toRemoveMap = lastTramUpdate.getOrPut(TramAction.REMOVE) {
//                        mutableMapOf()
//                    }
//                    val toUpdateMap = lastTramUpdate.getOrPut(TramAction.UPDATE) {
//                        mutableMapOf()
//                    }
//                    val toAddMap = lastTramUpdate.getOrPut(TramAction.ADD) {
//                        mutableMapOf()
//                    }
                    val newTramIds = operationResult.tramDataHashMap.map { it.id }
                    allKnownTrams.entries
                        .filter { (key) -> key !in newTramIds }
                        .onEach {
                            //                            toRemoveMap[it.key] = it.value
                            allKnownTrams.remove(it.key)
                        }

                    operationResult.tramDataHashMap.forEach { tramData ->
                        val existingTramMarker = allKnownTrams[tramData.id]
                        if (existingTramMarker != null) {
//                            toUpdateMap[tramData.id] = existingTramMarker
                            existingTramMarker.updatePosition(tramData.latLng)
                        } else {
                            val newTramMarker = TramMarker(tramData)
//                            toAddMap[tramData.id] = newTramMarker
                            allKnownTrams[tramData.id] = newTramMarker
                        }
                    }
                    showOrZoom(true)
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

    private fun showOrZoom(animate: Boolean) {
        val onlyVisibleTrams = allKnownTrams.values
            .filter { isMarkerLineVisible(it.tramLine) }
            .filter { tramMarker ->
                visibleRegion?.let { tramMarker.isOnMap(it) } ?: false
            }
        if (onlyVisibleTrams.size <= MAX_VISIBLE_MARKERS) {
            postVisibleMarkers(onlyVisibleTrams, animate)
        } else {
            _mapControls.postValue(MapControls.ZoomIn)
        }
    }

    private fun postVisibleMarkers(tramsToBeShown: List<TramMarker>, animate: Boolean) {
        _tramData.postValue(UiState.Success(tramsToBeShown, animate))
    }

    private fun isMarkerLineVisible(line: String) =
        !(favoriteView.value ?: false) || line in tramRepository.favoriteTrams.value ?: emptyList()

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
