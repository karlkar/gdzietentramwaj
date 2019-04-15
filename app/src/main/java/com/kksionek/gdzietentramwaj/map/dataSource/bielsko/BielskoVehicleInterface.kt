package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import io.reactivex.Single
import retrofit2.http.GET

interface BielskoVehicleInterface {

    // https://rozklady.bielsko.pl/getRunningVehicles.json

    @GET("/getRunningVehicles.json")
    fun vehicles(): Single<BielskoVehicleList>
}