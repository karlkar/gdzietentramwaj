package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.google.gson.annotations.SerializedName

data class GopFeature(
    @SerializedName("geometry")
    val geometry: GopGeometry,
//    @SerializedName("type")
//    val type: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("properties")
    val properties: GopProperties
)