package com.kksionek.gdzietentramwaj.dataSource

import com.google.gson.annotations.SerializedName
import java.util.*

data class TramList(
    @SerializedName("result")
    val list: ArrayList<TramData>
)
