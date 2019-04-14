package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.google.gson.annotations.SerializedName

data class WroclawVehicleList(
//    @SerializedName("resource_id")
//    val resourceId: String,
//    @SerializedName("fields")
//    val fields: List<Field>,
    @SerializedName("records")
    val records: List<Record>
//    @SerializedName("_links")
//    val links: Links,
//    @SerializedName("limit")
//    val limit: Int,
//    @SerializedName("total")
//    val total: Int
)