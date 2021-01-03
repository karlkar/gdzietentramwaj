package com.kksionek.gdzietentramwaj.main.viewModel

import android.app.Activity
import android.content.DialogInterface
import androidx.annotation.VisibleForTesting
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.play.core.install.model.AppUpdateType
import com.kksionek.gdzietentramwaj.addToDisposable
import com.kksionek.gdzietentramwaj.initWith
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

@VisibleForTesting
const val GOOGLE_API_AVAILABILITY_REQUEST_CODE = 2345

@VisibleForTesting
const val APP_UPDATE_AVAILABILITY_REQUEST_CODE = 7890

class MainViewModel @ViewModelInject constructor(
    private val locationRepository: LocationRepository,
    private val mapSettingsProvider: MapSettingsProvider,
    private val appUpdateRepository: AppUpdateRepository,
    private val googleApiAvailabilityChecker: GoogleApiAvailabilityChecker
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _locationPermission =
        MutableLiveData<Boolean>().initWith(locationRepository.isLocationPermissionGranted())
    val locationPermissionGrantedStatus: LiveData<Boolean> = _locationPermission

    private val _locationPermissionRequestor = MutableLiveData<Boolean>()
    val locationPermissionRequestor: LiveData<Boolean> = _locationPermissionRequestor

    private val _appUpdateAvailable = MutableLiveData<Boolean>().initWith(false)
    val appUpdateAvailable: LiveData<Boolean> = _appUpdateAvailable

    private val _lastLocation = MutableLiveData<LatLng>()
    val lastLocation: LiveData<LatLng> = _lastLocation

    init {
        subscribeToLastLocation()
        subscribeToAppUpdateAvailability()
    }

    private fun subscribeToLastLocation() {
        locationRepository.lastKnownLocation
            .onErrorReturnItem(mapSettingsProvider.getCity().latLng)
            .subscribe { location ->
                _lastLocation.postValue(location)
            }
            .addToDisposable(compositeDisposable)
    }

    private fun subscribeToAppUpdateAvailability() {
        appUpdateRepository.isUpdateAvailable()
            .doOnError { Timber.w(it, "Couldn't check app update availability") }
            .onErrorReturnItem(false)
            .subscribeOn(Schedulers.io())
            .subscribe { updateAvailability ->
                _appUpdateAvailable.postValue(updateAvailability)
            }
            .addToDisposable(compositeDisposable)
    }

    fun requestLocationPermission() {
        if (!locationRepository.isLocationPermissionGranted()) {
            _locationPermissionRequestor.postValue(true)
        }
    }

    fun onRequestPermissionsResult(granted: Boolean) {
        _locationPermission.postValue(granted)
    }

    fun startUpdateFlowForResult(activity: Activity) {
        appUpdateRepository.startUpdateFlowForResult(
            AppUpdateType.IMMEDIATE,
            activity,
            APP_UPDATE_AVAILABILITY_REQUEST_CODE
        )
    }

    fun onResume(activity: Activity) {
        appUpdateRepository.isUpdateInProgress()
            .doOnError { Timber.w(it, "Couldn't check if update is in progress") }
            .onErrorReturnItem(false)
            .subscribeOn(Schedulers.io())
            .subscribe { updateInProgress ->
                if (updateInProgress) {
                    startUpdateFlowForResult(activity)
                }
            }
            .addToDisposable(compositeDisposable)
    }

    fun showGoogleApiUpdateNeededDialog(activity: Activity, callback: ((DialogInterface) -> Unit)) {
        googleApiAvailabilityChecker.showGoogleApiUpdateNeededDialog(
            activity,
            GOOGLE_API_AVAILABILITY_REQUEST_CODE,
            callback
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}