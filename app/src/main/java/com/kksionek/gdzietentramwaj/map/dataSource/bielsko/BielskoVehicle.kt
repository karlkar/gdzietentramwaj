package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.google.gson.annotations.SerializedName

data class BielskoVehicle(
    @SerializedName("lineName")
    val line: String,
    @SerializedName("courseLoid")
    val id: Int,
//    @SerializedName("dayCourseLoid")
//    val dayCourseLoid: Int,
    @SerializedName("vehicleId")
    val brigade: String,
//    @SerializedName("delaySec")
//    val delaySec: Any,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("latitude")
    val latitude: Double,
//    @SerializedName("angle")
//    val angle: Int,
//    @SerializedName("reachedMeters")
//    val reachedMeters: Int,
//    @SerializedName("variantLoid")
//    val variantLoid: Int,
    @SerializedName("lastPingDate")
    val timestamp: Long
//    @SerializedName("distanceToNearestStopPoint")
//    val distanceToNearestStopPoint: Int,
//    @SerializedName("nearestSymbol")
//    val nearestSymbol: String,
//    @SerializedName("operator")
//    val operator: Any,
//    @SerializedName("onStopPoint")
//    val onStopPoint: String
) {
    fun isTram(): Boolean = false
}
