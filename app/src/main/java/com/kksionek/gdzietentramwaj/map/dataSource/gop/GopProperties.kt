package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.google.gson.annotations.SerializedName

data class GopProperties(
//    @SerializedName("html_portal_url")
//    val htmlPortalUrl: String,
    @SerializedName("code")
    val code: String,
//    @SerializedName("angle")
//    val angle: Double,
//    @SerializedName("html_ml_portal_url")
//    val htmlMlPortalUrl: String,
    @SerializedName("get_line_name")
    val line: String,
//    @SerializedName("html_mobile_portal_url")
//    val htmlMobilePortalUrl: String,
//    @SerializedName("_route_id")
//    val routeId: Int,
//    @SerializedName("_stop_id")
//    val stopId: Int,
//    @SerializedName("_post_id")
//    val postId: Int,
//    @SerializedName("get_line_id")
//    val getLineId: Int,
//    @SerializedName("get_trip_id")
//    val getTripId: Int,
//    @SerializedName("difference")
//    val difference: Int,
    @SerializedName("get_display_text")
    val brigade: String
)