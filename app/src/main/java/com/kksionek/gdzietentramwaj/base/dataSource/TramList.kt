package com.kksionek.gdzietentramwaj.base.dataSource

import com.google.gson.annotations.SerializedName
import java.util.ArrayList

data class TramList(
    @SerializedName("result")
    val list: ArrayList<TramData>
)
