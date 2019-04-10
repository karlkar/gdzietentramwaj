package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng

data class VehicleData(
    val id: String,
    val time: String,
    val latLng: LatLng,
    val firstLine: String,
    val brigade: String?
)