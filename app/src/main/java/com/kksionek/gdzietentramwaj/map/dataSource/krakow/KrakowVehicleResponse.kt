package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.google.gson.annotations.SerializedName

data class KrakowVehicleResponse(
    @SerializedName("lastUpdate")
    val lastUpdate: Long,
    @SerializedName("vehicles")
    val vehicles: List<KrakowVehicle>
)