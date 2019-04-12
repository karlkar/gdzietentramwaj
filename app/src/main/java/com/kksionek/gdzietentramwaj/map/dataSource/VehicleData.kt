package com.kksionek.gdzietentramwaj.map.dataSource

import com.google.android.gms.maps.model.LatLng

data class VehicleData(
    val id: String,
    val time: String,
    val latLng: LatLng,
    val firstLine: String,
    val isTram: Boolean,
    val brigade: String?
)