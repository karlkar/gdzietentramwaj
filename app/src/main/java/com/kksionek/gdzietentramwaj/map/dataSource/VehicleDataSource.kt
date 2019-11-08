package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single

interface VehicleDataSource {
    fun vehicles(): Single<List<VehicleData>>
}