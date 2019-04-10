package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.map.repository.VehicleData
import io.reactivex.Single

interface VehicleDataSource {
    fun buses(): Single<List<VehicleData>>
    fun trams(): Single<List<VehicleData>>
}