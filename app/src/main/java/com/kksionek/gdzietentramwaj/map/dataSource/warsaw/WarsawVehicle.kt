package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import androidx.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class WarsawVehicle(
    val id: String,
    val timestamp: String,
    val position: Position,
    val prevPosition: Position?,
    val line: String,
    val brigade: String,
    val isTram: Boolean
) {
    val latLng: LatLng
        get() = LatLng(position.latitude, position.longitude)

    val prevLatLng: LatLng?
        get() = prevPosition?.let { LatLng(it.latitude, it.longitude) }
}

@Keep
data class Position(val latitude: Double, val longitude: Double)
