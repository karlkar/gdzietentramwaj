package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers

interface MpkVehicleInterface {

    @Headers("Cache-Control: no-cache")
    @GET("/open-data/api/action/datastore_search?resource_id=17308285-3977-42f7-81b7-fdd168c210a2&limit=10000")
    fun buses(): Single<MpkVehicleResponse>
}