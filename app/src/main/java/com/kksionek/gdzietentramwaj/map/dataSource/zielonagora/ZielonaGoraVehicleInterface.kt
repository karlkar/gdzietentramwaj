package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import io.reactivex.Single
import retrofit2.http.GET

interface ZielonaGoraVehicleInterface {

    // http://traveller.mzk.zgora.pl/vm/main?command=planner&action=vehicles

    @GET("/vm/main?command=planner&action=vehicles")
    fun vehicles(): Single<List<ZielonaGoraVehicle>>
}