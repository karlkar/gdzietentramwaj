package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.google.gson.annotations.SerializedName

data class TtssVehicleResponse(
    @SerializedName("lastUpdate")
    val lastUpdate: Long,
    @SerializedName("vehicles")
    val vehicles: List<TtssVehicle>
)