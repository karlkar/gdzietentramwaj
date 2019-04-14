package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class WarsawVehicle(
    @SerializedName("Time")
    val time: String,
    @SerializedName("Lat")
    private val lat: Double,
    @SerializedName("Lon")
    private val lng: Double,
    @SerializedName("Lines")
    val firstLine: String,
    @SerializedName("Brigade")
    val brigade: String
) {
    val id: String
        get() = "$firstLine/$brigade"

    val latLng: LatLng
        get() = LatLng(lat, lng)

    fun isTram(): Boolean = firstLine.length < 3
}
