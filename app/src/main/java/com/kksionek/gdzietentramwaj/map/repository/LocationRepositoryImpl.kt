package com.kksionek.gdzietentramwaj.map.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.kksionek.gdzietentramwaj.main.model.GttLocation
import com.kksionek.gdzietentramwaj.main.model.Latitude
import com.kksionek.gdzietentramwaj.main.model.Longitude
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(private val context: Context): LocationRepository {

    @SuppressLint("MissingPermission") // when permission is missing we will get an error. It needs to be handled
    override val lastKnownLocation: Single<GttLocation> =
        Single.fromCallable {
            val providerClient = LocationServices.getFusedLocationProviderClient(context)
            Tasks.await(providerClient.lastLocation)
        }
            .map { GttLocation(Latitude(it.latitude), Longitude(it.longitude)) }
            .subscribeOn(Schedulers.io())

    override fun isLocationPermissionGranted(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
