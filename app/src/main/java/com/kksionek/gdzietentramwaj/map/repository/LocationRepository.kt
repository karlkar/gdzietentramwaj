package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.main.model.GttLocation
import io.reactivex.Single

interface LocationRepository {

    val lastKnownLocation: Single<GttLocation>

    fun isLocationPermissionGranted(): Boolean
}