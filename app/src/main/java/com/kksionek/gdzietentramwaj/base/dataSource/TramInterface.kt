package com.kksionek.gdzietentramwaj.base.dataSource

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers

const val ID = "f2e5503e-927d-4ad3-9500-4ab9e55deb59"
const val TYPE_BUS = 1
const val TYPE_TRAM = 2
const val APIKEY = "***REMOVED***"

// https://api.um.warszawa.pl/api/action/busestrams_get/?resource_id=f2e5503e-927d-4ad3-9500-4ab9e55deb59&apikey=***REMOVED***&type=2
interface TramInterface {

    @Headers("Cache-Control: no-cache")
    @GET("/api/action/busestrams_get/?resource_id=$ID&apikey=$APIKEY&type=$TYPE_BUS")
    fun buses(): Single<TramList>

    @Headers("Cache-Control: no-cache")
    @GET("/api/action/busestrams_get/?resource_id=$ID&apikey=$APIKEY&type=$TYPE_TRAM")
    fun trams(): Single<TramList>
}
