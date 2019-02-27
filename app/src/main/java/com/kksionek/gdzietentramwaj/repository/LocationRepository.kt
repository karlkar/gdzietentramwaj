package com.kksionek.gdzietentramwaj.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

private const val WARSAW_LAT = 52.231841
private const val WARSAW_LNG = 21.005940

class LocationRepository @Inject constructor(private val context: Context) {

    private val defaultLocation = Location("").apply {
        latitude = WARSAW_LAT
        longitude = WARSAW_LNG
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                    PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    val lastKnownLocation: Single<Location> =
        Single.fromCallable {
            if (isLocationPermissionGranted()) {
                val providerClient = LocationServices.getFusedLocationProviderClient(context)
                Tasks.await(providerClient.lastLocation)
            } else {
                defaultLocation
            }
        }
            .subscribeOn(Schedulers.io())
}
