package com.kksionek.gdzietentramwaj.map.dataSource.gop

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GopVehicleInterface {

    // http://sdip.metropoliaztm.pl/web/map/vehicles/gj/TB?interval=00%3A05%3A00&route_id=203&bbox=1851036.9468776,6256378.5785659,2366528.2655612,6765143.4387611,EPSG%3A3857

    @GET("/web/map/vehicles/gj/TB?interval=00%3A05%3A00&bbox=1851036.9468776,6256378.5785659,2366528.2655612,6765143.4387611,EPSG%3A3857")
    fun vehiclesTB(@Query("route_id") routeId: Int): Single<GopVehicleList>

    @GET("/web/map/vehicles/gj/A?interval=00%3A05%3A00&bbox=1851036.9468776,6256378.5785659,2366528.2655612,6765143.4387611,EPSG%3A3857")
    fun vehiclesA(@Query("route_id") routeId: Int): Single<GopVehicleList>

    @GET("/web/map/vehicles/gj/UNK?interval=00%3A05%3A00&bbox=1851036.9468776,6256378.5785659,2366528.2655612,6765143.4387611,EPSG%3A3857")
    fun vehiclesUNK(@Query("route_id") routeId: Int): Single<GopVehicleList>

    @GET("/web/map/vehicles/gj/T?interval=00%3A05%3A00&bbox=1851036.9468776,6256378.5785659,2366528.2655612,6765143.4387611,EPSG%3A3857")
    fun vehiclesT(@Query("route_id") routeId: Int): Single<GopVehicleList>

    @GET("/web/ml/line/")
    fun getRoutes(): Single<String>
}