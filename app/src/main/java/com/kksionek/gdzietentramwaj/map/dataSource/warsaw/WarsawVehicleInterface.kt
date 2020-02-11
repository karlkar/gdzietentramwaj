package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers

interface WarsawVehicleInterface {

    @Headers("Cache-Control: no-cache")
    @GET("/vehicles/list")
    fun vehicles(): Single<WarsawVehicleResponse>
}
