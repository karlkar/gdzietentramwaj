package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import io.reactivex.Single

interface LocationRepository {

    val lastKnownLocation: Single<LatLng>

    fun isLocationPermissionGranted(): Boolean
}