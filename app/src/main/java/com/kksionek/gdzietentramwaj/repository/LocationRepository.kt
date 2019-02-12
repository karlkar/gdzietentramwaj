package com.kksionek.gdzietentramwaj.repository

import android.content.Context
import android.location.Location

import com.google.android.gms.tasks.Task

import javax.inject.Inject

class LocationRepository @Inject constructor(context: Context) {
    private val mLocationGetter: LocationGetter = LocationGetter(context)

    val lastKnownLocation: Task<Location>
        get() = mLocationGetter.lastKnownLocation
}
