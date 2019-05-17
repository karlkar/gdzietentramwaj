package com.kksionek.gdzietentramwaj.main.viewModel

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.play.core.install.model.AppUpdateType
import com.kksionek.gdzietentramwaj.initWith
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.toLocation
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private const val GOOGLE_API_AVAILABILITY_REQUEST_CODE = 2345
private const val APP_UPDATE_AVAILABILIITY_REQUEST_CODE = 7890

class MainViewModel @Inject constructor(
    private val context: Context,
    private val locationRepository: LocationRepository,
    private val mapSettingsProvider: MapSettingsProvider,
    private val appUpdateRepository: AppUpdateRepository,
    private val googleApiAvailabilityChecker: GoogleApiAvailabilityChecker
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _locationPermission =
        MutableLiveData<Boolean>().initWith(isLocationPermissionGranted(context))
    val locationPermission: LiveData<Boolean> = _locationPermission

    private val _appUpdateAvailable = MutableLiveData<Boolean>().initWith(false)
    val appUpdateAvailable: LiveData<Boolean> = _appUpdateAvailable

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
        subscribeToAppUpdateAvailability()
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

    private fun subscribeToAppUpdateAvailability() {
        compositeDisposable.add(
            appUpdateRepository.isUpdateAvailable()
                .doOnError { Log.e(TAG, "Couldn't check app update availability", it) }
                .onErrorReturnItem(false)
                .subscribeOn(Schedulers.io())
                .subscribe { updateAvailability ->
                    _appUpdateAvailable.postValue(updateAvailability)
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

    fun startUpdateFlowForResult(activity: Activity) {
        appUpdateRepository.startUpdateFlowForResult(
            AppUpdateType.IMMEDIATE,
            activity,
            APP_UPDATE_AVAILABILIITY_REQUEST_CODE
        )
    }

    fun onResume(activity: Activity) {
        compositeDisposable.add(appUpdateRepository.isUpdateInProgress()
            .doOnError { Log.e(TAG, "Couldn't check if update is in progress", it) }
            .onErrorReturnItem(false)
            .subscribeOn(Schedulers.io())
            .subscribe { updateInProgress ->
                if (updateInProgress) {
                    startUpdateFlowForResult(activity)
                }
            })
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

    companion object {
        const val TAG = "MainViewModel"
    }
}