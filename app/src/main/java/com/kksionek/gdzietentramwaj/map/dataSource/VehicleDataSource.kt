package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single

interface VehicleDataSource {
    fun vehicles(): Single<List<VehicleData>>
//    fun buses(): Single<List<VehicleData>>
//    fun trams(): Single<List<VehicleData>>
}