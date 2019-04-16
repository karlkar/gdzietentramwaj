package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.google.gson.annotations.SerializedName

data class ZielonaGoraVehicle(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
//    @SerializedName("time") // is always 0
//    val time: Int,
//    @SerializedName("depid")
//    val depid: Int,
//    @SerializedName("type")
//    val type: String
)
