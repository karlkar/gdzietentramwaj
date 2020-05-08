package com.kksionek.gdzietentramwaj.map.model

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class VehicleToDrawData(
    val id: String,
    var position: LatLng,
    val line: String,
    val isTram: Boolean,
    val brigade: String? = null,
    var prevPosition: LatLng? = null,
    val icon: BitmapDescriptor? = null
) {

    constructor(vehicleData: VehicleData) : this(
        vehicleData.id,
        vehicleData.position,
        vehicleData.line,
        vehicleData.isTram,
        vehicleData.brigade,
        vehicleData.prevPosition
    )

    fun isOnMap(bounds: LatLngBounds): Boolean = bounds.contains(position)
            || (prevPosition?.let { bounds.contains(it) } ?: false)

    fun updatePosition(newFinalPosition: LatLng) {
        if (newFinalPosition == position)
            prevPosition = null
        else {
            prevPosition = position
            position = newFinalPosition
        }
    }
}