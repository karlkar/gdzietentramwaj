package com.kksionek.gdzietentramwaj.map.viewModel

import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.JsonSyntaxException
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.dataSource.MapTypes
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.repository.DifficultiesRepository
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.TramRepository
import com.kksionek.gdzietentramwaj.map.repository.VehicleData
import com.kksionek.gdzietentramwaj.map.view.BusTramLoading
import com.kksionek.gdzietentramwaj.map.view.MapControls
import com.kksionek.gdzietentramwaj.map.view.TramMarker
import com.kksionek.gdzietentramwaj.map.view.UiState
import com.kksionek.gdzietentramwaj.toLatLng
import com.kksionek.gdzietentramwaj.toLocation
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException
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
    private val difficultiesRepository: DifficultiesRepository,
    private val crashReportingService: CrashReportingService,
    val iconSettingsProvider: IconSettingsProvider,
    val mapSettingsManager: MapSettingsManager
) : ViewModel() {

    object NoTramsLoadedException : Throwable()

    val favoriteView = MutableLiveData<Boolean>().apply {
        value = mapsViewSettingsRepository.isFavoriteTramViewEnabled()
    }

    var followedVehicle: FollowedTramData? by Delegates.observable(null) { _, _: FollowedTramData?, value: FollowedTramData? ->
        value?.let {
            _mapControls.postValue(MapControls.MoveTo(it.latLng))
        }
    }

    private val _mapControls = MutableLiveData<MapControls>()
    val mapControls: LiveData<MapControls> = _mapControls

    private var allKnownTrams = mapOf<String, TramMarker>()
    private var allTrams: List<TramMarker> = emptyList()

    private val _tramData = MutableLiveData<UiState<BusTramLoading>>()
    val tramData: LiveData<UiState<BusTramLoading>> = _tramData

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location> = _lastLocation

    private val _difficulties = MutableLiveData<UiState<List<DifficultiesEntity>>>()
    val difficulties: LiveData<UiState<List<DifficultiesEntity>>> = _difficulties

    var visibleRegion by Delegates.observable<LatLngBounds?>(null) { _, _, _ ->
        showOrZoom(false)
    }

    private val compositeDisposable = CompositeDisposable()
    private var tramFetchingDisposable: Disposable? = null
    private var difficultiesDisposable: Disposable? = null

    private val favoriteLock = ReentrantReadWriteLock()
    private var favoriteTrams = emptyList<String>()

    private val selectedCity: Cities
        get() = mapSettingsManager.getCity()

    val mapInitialPosition: LatLng
    val mapInitialZoom: Float

    init {
        val defaultLocation = selectedCity.latLng
        val defaultZoom = mapSettingsManager.getDefaultZoom()
        if (mapSettingsManager.isStartLocationEnabled()) {
            mapInitialPosition = mapSettingsManager.getStartLocationPosition() ?: defaultLocation
            mapInitialZoom = mapSettingsManager.getStartLocationZoom() ?: defaultZoom
        } else {
            mapInitialPosition = defaultLocation
            mapInitialZoom = defaultZoom
        }

        observeFavoriteTrams()
        forceReloadDifficulties()
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

    // This one has to be called when the map is ready to use
    fun forceReloadLastLocation() {
        compositeDisposable.add(locationRepository.lastKnownLocation
            .onErrorReturnItem(selectedCity.latLng.toLocation())
            .subscribe { location ->
                if (!mapSettingsManager.isStartLocationEnabled()) {
                    _mapControls.postValue(MapControls.MoveTo(location.toLatLng()))
                }
                _lastLocation.postValue(location)
            })
    }

    private fun startFetchingTrams() {
        tramFetchingDisposable?.dispose()
        tramFetchingDisposable = tramRepository.dataStream(selectedCity)
            .subscribeOn(Schedulers.io())
            .transformEmptyListToError()
            .subscribe { operationResult ->
                when (operationResult) {
                    is NetworkOperationResult.Success<List<VehicleData>> ->
                        handleSuccess(operationResult)
                    is NetworkOperationResult.Error ->
                        handleError(operationResult)
                    is NetworkOperationResult.InProgress ->
                        _tramData.postValue(UiState.InProgress())
                }.makeExhaustive
            }
    }

    private fun handleSuccess(operationResult: NetworkOperationResult.Success<List<VehicleData>>) {
        allTrams = operationResult.data
            .map { allKnownTrams[it.id]?.apply { updatePosition(it.latLng) } ?: TramMarker(it) }
        allKnownTrams = allTrams.associateBy { it.id }

        showOrZoom(animate = true, newData = true)
    }

    private fun handleError(operationResult: NetworkOperationResult.Error<List<VehicleData>>) {
        val uiState: UiState.Error<BusTramLoading> = when (operationResult.throwable) {
            NoTramsLoadedException -> UiState.Error(R.string.none_position_is_up_to_date)
            is UnknownHostException, is SocketTimeoutException -> UiState.Error(R.string.error_internet)
            else -> {
                if (operationResult.throwable is CompositeException
                    && operationResult.throwable.exceptions.all { it is HttpException }
                ) {
                    UiState.Error(R.string.error_internet)
                } else {
                    if (operationResult.throwable !is JsonSyntaxException
                        && operationResult.throwable !is IllegalStateException
                        && operationResult.throwable !is HttpException
                        && (operationResult.throwable is CompositeException
                                && (operationResult.throwable.exceptions.any { it !is HttpException }))
                    ) {
                        crashReportingService.reportCrash(
                            operationResult.throwable,
                            "Error handled with a toast."
                        )
                    }
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Exception", operationResult.throwable)
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
        }
        _tramData.postValue(uiState)
    }

    private fun Flowable<NetworkOperationResult<List<VehicleData>>>.transformEmptyListToError() =
        map {
            if (it is NetworkOperationResult.Success<List<VehicleData>> && it.data.isEmpty()) {
                NetworkOperationResult.Error(NoTramsLoadedException)
            } else {
                it
            }
        }

    private fun showOrZoom(animate: Boolean, newData: Boolean = false) {
        val onlyVisibleTrams = allTrams
            .filter { isMarkerLineVisible(it.tramLine) }
            .filter { visibleRegion?.let { region -> it.isOnMap(region) } ?: false }

        if (onlyVisibleTrams.size <= MAX_VISIBLE_MARKERS) {
            postMarkers(onlyVisibleTrams, animate, newData)
        } else if (mapSettingsManager.isAutoZoomEnabled()) {
            _mapControls.postValue(MapControls.ZoomIn)
        } else {
            _mapControls.postValue(MapControls.IgnoredZoomIn(R.string.map_auto_zoom_disabled_message))
        }

        val followed = followedVehicle
        if (followed != null && animate) {
            allTrams.firstOrNull { it.id == followed.id }?.let { tramMarker ->
                _mapControls.postValue(MapControls.MoveTo(tramMarker.finalPosition, true))
            }
        }
    }

    private fun postMarkers(
        visibleTrams: List<TramMarker>,
        animate: Boolean,
        newData: Boolean = false
    ) {
        _tramData.postValue(UiState.Success(BusTramLoading(visibleTrams, animate, newData)))
    }

    private fun isMarkerLineVisible(line: String): Boolean = favoriteLock.read {
        !(favoriteView.value ?: false) || line in favoriteTrams
    }

    private fun stopFetchingTrams() {
        tramFetchingDisposable?.dispose()
    }

    fun toggleFavorite() {
        val favoriteViewOn = !(favoriteView.value ?: return)
        mapsViewSettingsRepository.saveFavoriteTramViewState(favoriteViewOn)
        favoriteView.value = favoriteViewOn
        showOrZoom(false)
    }

    fun forceReloadTrams() {
        tramRepository.forceReload()
    }

    fun forceReloadDifficulties() {
        val city = selectedCity
        difficultiesDisposable?.dispose()
        if (!difficultiesRepository.supportsDifficulties(city)) {
            _difficulties.postValue(null)
            return
        }
        difficultiesDisposable = difficultiesRepository.getDifficulties(city)
            .map { result ->
                when (result) {
                    is NetworkOperationResult.Success -> UiState.Success(result.data)
                    is NetworkOperationResult.Error -> {
                        Log.e(TAG, "Failed to reload difficulties", result.throwable)
                        if (result.throwable !is HttpException) {
                            crashReportingService.reportCrash(
                                result.throwable,
                                "Failed to reload difficulties"
                            )
                        }
                        UiState.Error<List<DifficultiesEntity>>(R.string.error_failed_to_reload_difficulties)
                    }
                    is NetworkOperationResult.InProgress -> UiState.InProgress()
                }
            }
            .subscribe { _difficulties.postValue(it) }
    }

    fun onSwitchMapTypeButtonClicked() {
        val currentMapType = mapSettingsManager.getMapType()
        val newMapType = currentMapType.next()
        mapSettingsManager.setMapType(newMapType)
        _mapControls.postValue(MapControls.ChangeType(newMapType))
    }

    fun getMapType(): MapTypes = mapSettingsManager.getMapType()

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
