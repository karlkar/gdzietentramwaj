package com.kksionek.gdzietentramwaj.map.repository

import android.location.Location
import io.reactivex.Single

interface LocationRepository {

    val lastKnownLocation: Single<Location>

    fun isLocationPermissionGranted(): Boolean
}