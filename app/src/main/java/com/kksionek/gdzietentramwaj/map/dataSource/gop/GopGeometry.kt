package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.google.gson.annotations.SerializedName

data class GopGeometry(
    @SerializedName("type")
    val type: String,
    @SerializedName("coordinates")
    val coordinates: List<Double>
)