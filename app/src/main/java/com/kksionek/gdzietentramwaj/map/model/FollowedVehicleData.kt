package com.kksionek.gdzietentramwaj.map.model

import com.google.android.gms.maps.model.LatLng

data class FollowedVehicleData(
    val id: String,
    val title: String,
    val snippet: String,
    val latLng: LatLng
)