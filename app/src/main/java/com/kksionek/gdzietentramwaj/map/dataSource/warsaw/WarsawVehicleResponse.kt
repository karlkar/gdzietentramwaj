package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.gson.annotations.SerializedName

data class WarsawVehicleResponse(
    @SerializedName("result")
    val list: List<WarsawVehicle>
)
