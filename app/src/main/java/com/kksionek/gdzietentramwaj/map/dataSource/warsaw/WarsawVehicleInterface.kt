package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

const val ID = "f2e5503e-927d-4ad3-9500-4ab9e55deb59"
const val TYPE_BUS = 1
const val TYPE_TRAM = 2

// https://api.um.warszawa.pl/api/action/busestrams_get/?resource_id=f2e5503e-927d-4ad3-9500-4ab9e55deb59&apikey=***REMOVED***&type=2
interface WarsawVehicleInterface {

    @Headers("Cache-Control: no-cache")
    @GET("/api/action/busestrams_get/?resource_id=$ID&type=$TYPE_BUS")
    fun buses(@Query("apikey") apikey: String): Single<WarsawVehicleResponse>

    @Headers("Cache-Control: no-cache")
    @GET("/api/action/busestrams_get/?resource_id=$ID&type=$TYPE_TRAM")
    fun trams(@Query("apikey") apikey: String): Single<WarsawVehicleResponse>
}
