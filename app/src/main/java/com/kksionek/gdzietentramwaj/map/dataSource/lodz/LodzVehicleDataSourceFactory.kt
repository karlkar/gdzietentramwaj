package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

const val BASE_URL = "http://rozklady.lodz.pl/"

class LodzVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): VehicleDataSource {
        val lodzVehicleInterface = interfaceBuilder.create(BASE_URL, LodzVehicleInterface::class)
        return LodzVehicleDataSource(lodzVehicleInterface)
    }
}