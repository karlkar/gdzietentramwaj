package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.tasks.Task
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
import javax.inject.Inject
import kotlin.properties.Delegates

private const val MAX_VISIBLE_MARKERS = 50

class MapsViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository
) : ViewModel() {

    enum class TramAction {
        ADD,
        REMOVE,
        UPDATE
    }

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
                    _tramData.postValue(
                        UiState.Error(
                            operationResult.throwable,
                            true
                        )
                    ) //TODO logic for showing
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
