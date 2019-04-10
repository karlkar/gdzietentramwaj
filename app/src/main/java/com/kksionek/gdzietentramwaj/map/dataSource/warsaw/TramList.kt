package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class TramList(
    @SerializedName("result")
    val list: ArrayList<TramData>
)
