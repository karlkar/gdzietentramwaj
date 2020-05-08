package com.kksionek.gdzietentramwaj.map.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.JsonSyntaxException
import com.google.maps.android.SphericalUtil
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.addToDisposable
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.initWith
import com.kksionek.gdzietentramwaj.makeExhaustive
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.MapTypes
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.kksionek.gdzietentramwaj.map.model.VehicleToDrawData
import com.kksionek.gdzietentramwaj.map.repository.DifficultiesRepository
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.VehiclesRepository
import com.kksionek.gdzietentramwaj.map.view.BusTramLoading
import com.kksionek.gdzietentramwaj.map.view.MapControls
import com.kksionek.gdzietentramwaj.map.view.UiState
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantReadWriteLock
import javax.inject.Inject
import kotlin.concurrent.read
import kotlin.concurrent.write
import kotlin.properties.Delegates

private const val MAX_VISIBLE_MARKERS = 50
private const val CITY_CHANGE_CHECK_THRESHOLD = 25000.0

class MapsViewModel @Inject constructor(
    private val vehiclesRepository: VehiclesRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository,
    private val difficultiesRepository: DifficultiesRepository,
    private val crashReportingService: CrashReportingService,
    private val iconSettingsProvider: IconSettingsProvider,
    private val mapSettingsManager: MapSettingsManager
) : ViewModel() {

    val favoriteView =
        MutableLiveData<Boolean>().initWith(mapsViewSettingsRepository.isFavoriteTramViewEnabled())

    var followedVehicle: FollowedTramData? by Delegates.observable(null) { _, _: FollowedTramData?, value: FollowedTramData? ->
        value?.let {
            _mapControls.postValue(MapControls.MoveTo(it.latLng))
        }
    }

    private val _mapControls = MutableLiveData<MapControls>()
    val mapControls: LiveData<MapControls> = _mapControls

    private var allKnownTrams = mapOf<String, VehicleToDrawData>()

    private val _tramData = MutableLiveData<UiState<BusTramLoading>>()
    val tramData: LiveData<UiState<BusTramLoading>> = _tramData

    private val _difficulties = MutableLiveData<UiState<DifficultiesState>>()
    val difficulties: LiveData<UiState<DifficultiesState>> = _difficulties

    var visibleRegion by Delegates.observable<LatLngBounds?>(null) { _, _, newValue ->
        newValue?.let { latLngBounds ->
            val nearestCity = getNearestCity(latLngBounds)
            val selectedCity = mapSettingsManager.getCity()
            if (selectedCity != nearestCity) {
                mapSettingsManager.setCity(nearestCity)
                subscribeToAllData()
            }
        }

        showOrZoom(false)
    }

    private fun getNearestCity(latLngBounds: LatLngBounds): Cities {
        val currentCity = mapSettingsManager.getCity()
        val distanceFromCurrentCity =
            SphericalUtil.computeDistanceBetween(latLngBounds.center, currentCity.latLng)
        return if (distanceFromCurrentCity <= CITY_CHANGE_CHECK_THRESHOLD) {
            currentCity
        } else {
            Cities.values()
                .minBy { SphericalUtil.computeDistanceBetween(it.latLng, latLngBounds.center) }
                ?: Cities.WARSAW
        }
    }

    private val compositeDisposable = CompositeDisposable()

    private val favoriteLock = ReentrantReadWriteLock()
    private var favoriteTrams = emptyList<String>()

    val mapInitialPosition: LatLng
    val mapInitialZoom: Float

    init {
        val defaultLocation = mapSettingsManager.getCity().latLng
        val defaultZoom = mapSettingsManager.getDefaultZoom()
        if (mapSettingsManager.isStartLocationEnabled()) {
            mapInitialPosition = mapSettingsManager.getStartLocationPosition() ?: defaultLocation
            mapInitialZoom = mapSettingsManager.getStartLocationZoom() ?: defaultZoom
        } else {
            mapInitialPosition = defaultLocation
            mapInitialZoom = defaultZoom
        }

        subscribeToLastLocation()
    }

    fun reloadLastLocation() {
        subscribeToLastLocation()
    }

    private fun subscribeToLastLocation() {
        locationRepository.lastKnownLocation
            .subscribe(
                { location ->
                    if (!mapSettingsManager.isStartLocationEnabled()) {
                        _mapControls.postValue(MapControls.MoveTo(location))
                    }
                },
                { throwable ->
                    if (throwable !is ExecutionException || throwable.cause !is SecurityException) {
                        Timber.w(throwable, "Failed to get last location")
                        crashReportingService.reportCrash(
                            throwable,
                            "Failed to get last location"
                        )
                    }
                })
            .addToDisposable(compositeDisposable)

    }

    private fun subscribeToAllData() {
        compositeDisposable.clear()
        val selectedCity = mapSettingsManager.getCity()
        subscribeToFavoriteTrams(selectedCity)
        subscribeToDifficulties(selectedCity)
        subscribeToVehicles(selectedCity)
    }

    private fun subscribeToFavoriteTrams(city: Cities) {
        vehiclesRepository.getFavoriteVehicleLines(city)
            .subscribeOn(Schedulers.io())
            .onErrorReturn { throwable: Throwable ->
                Timber.e(throwable, "Failed getting all the favorites from the database")
                crashReportingService.reportCrash(
                    throwable,
                    "Failed getting the favorite data from database"
                )
                emptyList()
            }
            .subscribe(
                Consumer {
                    favoriteLock.write {
                        favoriteTrams = it
                    }
                })
            .addToDisposable(compositeDisposable)
    }

    private fun subscribeToDifficulties(city: Cities) {
        difficultiesRepository.dataStream(city)
            .map { result ->
                when (result) {
                    is NetworkOperationResult.Success -> UiState.Success(result.data)
                    is NetworkOperationResult.Error -> {
                        Timber.e(result.throwable, "Failed to reload difficulties")
                        if (result.throwable !is HttpException) {
                            crashReportingService.reportCrash(
                                result.throwable,
                                "Failed to reload difficulties"
                            )
                        }
                        UiState.Error<DifficultiesState>(R.string.difficulties_error_failed_to_reload_difficulties)
                    }
                    is NetworkOperationResult.InProgress -> UiState.InProgress()
                }
            }
            .subscribe {
                _difficulties.postValue(it)
            }
            .addToDisposable(compositeDisposable)
    }

    private fun subscribeToVehicles(city: Cities) {
        vehiclesRepository.dataStream(city)
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
            .addToDisposable(compositeDisposable)
    }

    private fun Flowable<NetworkOperationResult<List<VehicleData>>>.transformEmptyListToError() =
        map {
            if (it is NetworkOperationResult.Success<List<VehicleData>> && it.data.isEmpty()) {
                NetworkOperationResult.Error(NoTramsLoadedException)
            } else {
                it
            }
        }

    private fun handleSuccess(operationResult: NetworkOperationResult.Success<List<VehicleData>>) {
        val allTrams = operationResult.data
            .map {
                allKnownTrams[it.id]
                    ?.apply { updatePosition(it.position) }
                    ?: VehicleToDrawData(it)
            }
        allKnownTrams = allTrams.associateBy { it.id }

        showOrZoom(animate = true, newData = true)
    }

    private fun handleError(operationResult: NetworkOperationResult.Error<List<VehicleData>>) {
        val uiState: UiState.Error<BusTramLoading> = when (operationResult.throwable) {
            NoTramsLoadedException -> UiState.Error(R.string.map_none_position_is_up_to_date)
            is UnknownHostException, is SocketTimeoutException -> UiState.Error(R.string.map_error_internet)
            else -> {
                if (operationResult.throwable is CompositeException
                    && operationResult.throwable.exceptions.all { it is HttpException }
                ) {
                    UiState.Error(R.string.map_error_internet)
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
                        Timber.e(operationResult.throwable, "Exception")
                        UiState.Error(
                            R.string.map_debug_error_message,
                            listOf(
                                operationResult.throwable.javaClass.simpleName,
                                operationResult.throwable.message
                                    ?: "null"
                            )
                        )
                    } else {
                        UiState.Error(R.string.map_error_ztm)
                    }
                }
            }
        }
        _tramData.postValue(uiState)
    }

    private fun showOrZoom(animate: Boolean, newData: Boolean = false) {
        val onlyVisibleTrams = allKnownTrams.values
            .filter { isVehicleVisible(it.line) }
            .filter { visibleRegion?.let { region -> it.isOnMap(region) } ?: false }

        when {
            onlyVisibleTrams.size <= MAX_VISIBLE_MARKERS ->
                _tramData.postValue(
                    UiState.Success(BusTramLoading(onlyVisibleTrams, animate, newData))
                )
            mapSettingsManager.isAutoZoomEnabled() ->
                _mapControls.postValue(MapControls.ZoomIn)
            else ->
                _mapControls.postValue(MapControls.IgnoredZoomIn(R.string.map_auto_zoom_disabled_message))
        }

        val followed = followedVehicle
        if (followed != null && animate) {
            allKnownTrams.values.firstOrNull { it.id == followed.id }?.let { tramMarker ->
                _mapControls.postValue(MapControls.MoveTo(tramMarker.position, true))
            }
        }
    }

    private fun isVehicleVisible(line: String): Boolean = favoriteLock.read {
        favoriteView.value != true || line in favoriteTrams
    }

    fun forceReloadTrams() {
        vehiclesRepository.forceReload()
    }

    fun forceReloadDifficulties() {
        difficultiesRepository.forceReload()
    }

    fun onToggleFavorite() {
        val favoriteViewOn = !(favoriteView.value ?: return)
        mapsViewSettingsRepository.saveFavoriteTramViewState(favoriteViewOn)
        favoriteView.value = favoriteViewOn
        showOrZoom(false)
    }

    fun onSwitchMapTypeButtonClicked() {
        val currentMapType = mapSettingsManager.getMapType()
        val newMapType = currentMapType.next()
        mapSettingsManager.setMapType(newMapType)
        _mapControls.postValue(MapControls.ChangeType(newMapType))
    }

    fun getMapType(): MapTypes = mapSettingsManager.getMapType()

    val isOldIconSetEnabled: Boolean
        get() = iconSettingsProvider.isOldIconSetEnabled()

    val isTrafficShowingEnabled: Boolean
        get() = mapSettingsManager.isTrafficShowingEnabled()

    val isBrigadeShowingEnabled: Boolean
        get() = mapSettingsManager.isBrigadeShowingEnabled()

    fun onResume() {
        subscribeToAllData()
    }

    fun onPause() {
        compositeDisposable.clear()
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}
