package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.google.gson.annotations.SerializedName

data class BielskoVehicleList(
    @SerializedName("vehicles")
    val list: List<BielskoVehicle>
)
