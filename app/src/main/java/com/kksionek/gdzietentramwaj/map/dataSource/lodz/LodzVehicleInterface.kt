package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import io.reactivex.Single
import retrofit2.http.POST

interface LodzVehicleInterface {

    @POST("/Home/CNR_GetVehicles")
    fun vehicles(): Single<String>
}