package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.google.gson.annotations.SerializedName

data class MpkVehicleResponse(
//    @SerializedName("help")
//    val help: String,
//    @SerializedName("success")
//    val success: Boolean,
    @SerializedName("result")
    val result: MpkVehicleList
)