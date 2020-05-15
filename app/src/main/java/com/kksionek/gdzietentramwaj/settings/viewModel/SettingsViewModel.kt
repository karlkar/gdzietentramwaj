package com.kksionek.gdzietentramwaj.settings.viewModel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val iconSettingsManager: IconSettingsManager,
    private val mapSettingsManager: MapSettingsManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    var oldIconSetEnabled: Boolean
        get() = iconSettingsManager.isOldIconSetEnabled()
        set(value) {
            iconSettingsManager.setIsOldIconSetEnabled(value)
        }

    var autoZoomEnabled: Boolean
        get() = mapSettingsManager.isAutoZoomEnabled()
        set(value) {
            mapSettingsManager.setAutoZoomEnabled(value)
        }

    fun saveStartLocation(location: LatLng, zoom: Float) {
        mapSettingsManager.setStartLocation(location, zoom)
    }

    var startLocationEnabled: Boolean
        get() = mapSettingsManager.isStartLocationEnabled()
        set(value) = mapSettingsManager.setStartLocationEnabled(value)

    val startLocationPosition: LatLng?
        get() = mapSettingsManager.getStartLocationPosition()

    val startLocationZoom: Float?
        get() = mapSettingsManager.getStartLocationZoom()

    var brigadeShowingEnabled: Boolean
        get() = mapSettingsManager.isBrigadeShowingEnabled()
        set(value) = mapSettingsManager.setBrigadeShowingEnabled(value)

    var trafficShowingEnabled: Boolean
        get() = mapSettingsManager.isTrafficShowingEnabled()
        set(value) = mapSettingsManager.setTrafficShowingEnabled(value)

    val city: Cities
        get() = mapSettingsManager.getCity()

    // As there is no "startFragmentForResult" I have to do a workaround
    var locationChooserFragmentClosedWithResult: Boolean = false

    val locationPermissionGranted: Boolean
        get() = locationRepository.isLocationPermissionGranted()
}