package com.kksionek.gdzietentramwaj.main.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.initWith
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.toLocation
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val mapSettingsProvider: MapSettingsProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _locationPermission =
        MutableLiveData<Boolean>().initWith(isLocationPermissionGranted(context))
    val locationPermission: LiveData<Boolean> = _locationPermission

    private fun isLocationPermissionGranted(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val _locationPermissionRequestLiveData = MutableLiveData<Boolean>()
    val locationPermissionRequestLiveData: LiveData<Boolean> = _locationPermissionRequestLiveData

    private val _lastLocation = MutableLiveData<Location>()
    val lastLocation: LiveData<Location> = _lastLocation

    init {
        subscribeToLastLocation()
    }

    private fun subscribeToLastLocation() {
        compositeDisposable.add(
            locationRepository.lastKnownLocation
                .onErrorReturnItem(mapSettingsProvider.getCity().latLng.toLocation())
                .subscribe { location ->
                    _lastLocation.postValue(location)
                }
        )
    }

    fun requestLocationPermission() {
        if (!isLocationPermissionGranted(context)) {
            _locationPermissionRequestLiveData.postValue(true)
        }
    }

    fun updateLocationPermission(granted: Boolean) {
        _locationPermission.postValue(granted)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}