package com.kksionek.gdzietentramwaj.dataSource

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class TramData(
    @SerializedName("Time")
    val time: String,
    @SerializedName("Lat")
    private val lat: Double,
    @SerializedName("Lon")
    private val lng: Double,
    @SerializedName("Lines")
    val firstLine: String,
    @SerializedName("Brigade")
    private val brigade: String
) {
    val id: String
        get() = "$firstLine/$brigade"

    private var mLatLng: LatLng? = null
    val latLng: LatLng
        get() = mLatLng ?: LatLng(lat, lng).also { mLatLng = it }
}
