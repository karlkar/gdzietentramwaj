package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.google.gson.annotations.SerializedName

data class GopVehicleList(

//    @SerializedName("crs")
//    val crs: Crs,
//    @SerializedName("type")
//    val type: String,
    @SerializedName("features")
    val features: List<GopFeature>
)