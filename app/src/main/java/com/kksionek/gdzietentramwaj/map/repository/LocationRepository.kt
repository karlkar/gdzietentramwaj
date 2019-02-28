package com.kksionek.gdzietentramwaj.map.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class LocationRepository @Inject constructor(private val context: Context) {

    @SuppressLint("MissingPermission") // when permission is missing we will get an error. It needs to be handled
    val lastKnownLocation: Single<Location> =
        Single.fromCallable {
            val providerClient = LocationServices.getFusedLocationProviderClient(context)
            Tasks.await(providerClient.lastLocation)
        }
            .subscribeOn(Schedulers.io())
}
