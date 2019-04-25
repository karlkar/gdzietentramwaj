package com.kksionek.gdzietentramwaj.main.viewModel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class MainViewModel @Inject constructor(
    context: Context
) : ViewModel() {

    private val _locationPermission = MutableLiveData<Boolean>().apply {
        value = Build.VERSION.SDK_INT <= Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    val locationPermission: LiveData<Boolean> = _locationPermission

    private val _requestLocationPermission = MutableLiveData<Boolean>()
    val requestLocationPermission: LiveData<Boolean> = _requestLocationPermission

    fun requestLocationPermission() {
        _requestLocationPermission.postValue(true)
    }

    fun updateLocationPermission(granted: Boolean) {
        _locationPermission.postValue(granted)
    }
}