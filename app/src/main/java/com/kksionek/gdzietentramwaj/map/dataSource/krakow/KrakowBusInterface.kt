package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers

interface KrakowBusInterface {

    // http://91.223.13.70/internetservice/geoserviceDispatcher/services/vehicleinfo/vehicles
    @Headers("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")
    @GET("/internetservice/geoserviceDispatcher/services/vehicleinfo/vehicles")
    fun buses(): Single<KrakowVehicleResponse>
}