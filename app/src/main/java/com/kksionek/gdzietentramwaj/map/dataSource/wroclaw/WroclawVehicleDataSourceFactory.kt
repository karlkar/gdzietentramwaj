package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val BASE_URL = "https://www.wroclaw.pl"

class WroclawVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {
    fun create(): VehicleDataSource {
        val mpkVehicleInterface = interfaceBuilder.create(BASE_URL, WroclawVehicleInterface::class)
        return WroclawVehicleDataSource(mpkVehicleInterface)
    }
}