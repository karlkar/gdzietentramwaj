package com.kksionek.gdzietentramwaj.main.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.initWith
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val context: Context
) : ViewModel() {

    private val _locationPermission =
        MutableLiveData<Boolean>().initWith(isLocationPermissionGranted(context))
    val locationPermission: LiveData<Boolean> = _locationPermission

    private fun isLocationPermissionGranted(context: Context): Boolean {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val _locationPermissionRequestLiveData = MutableLiveData<Boolean>()
    val locationPermissionRequestLiveData: LiveData<Boolean> = _locationPermissionRequestLiveData

    fun requestLocationPermission() {
        if (!isLocationPermissionGranted(context)) {
            _locationPermissionRequestLiveData.postValue(true)
        }
    }

    fun updateLocationPermission(granted: Boolean) {
        _locationPermission.postValue(granted)
    }
}