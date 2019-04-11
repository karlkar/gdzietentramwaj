package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class ZtmVehicleResponse(
    @SerializedName("result")
    val list: ArrayList<ZtmVehicle>
)
