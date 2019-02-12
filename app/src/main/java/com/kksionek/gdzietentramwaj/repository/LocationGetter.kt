package com.kksionek.gdzietentramwaj.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

internal class LocationGetter(context: Context) {
    private val providerClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val lastKnownLocation: Task<Location>
        @SuppressLint("MissingPermission")
        get() = providerClient.lastLocation
}
