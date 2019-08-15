package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface KrakowTramInterface {

    // https://mpk.jacekk.net/proxy_tram.php/geoserviceDispatcher/services/vehicleinfo/vehicles?positionType=RAW&colorType=ROUTE_BASED&lastUpdate=1565894264079
    // dawniej - http://www.ttss.krakow.pl/internetservice/geoserviceDispatcher/services/vehicleinfo/vehicles
    @Headers("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
    @GET("/proxy_tram.php/geoserviceDispatcher/services/vehicleinfo/vehicles")
    fun trams(
        @Query("lastUpdate") lastUpdate: Long = 0,
        @Query("positionType") positionType: String = "RAW",
        @Query("colorType") colorType: String = "ROUTE_BASED"
    ): Single<KrakowVehicleResponse>
}