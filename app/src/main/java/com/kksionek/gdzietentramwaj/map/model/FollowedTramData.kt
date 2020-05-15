package com.kksionek.gdzietentramwaj.map.model

import com.google.android.gms.maps.model.LatLng

data class FollowedTramData(
    val id: String,
    val title: String,
    val snippet: String,
    val latLng: LatLng
)