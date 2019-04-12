package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single

interface VehicleDataSource {
    fun vehicles(): Single<List<VehicleData>>
}