package com.kksionek.gdzietentramwaj.map.dataSource

import com.google.android.gms.maps.model.LatLng

data class VehicleData(
    val id: String,
    val position: LatLng,
    val line: String,
    val isTram: Boolean,
    val brigade: String? = null,
    val prevPosition: LatLng? = null
)