package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val BASE_URL = "https://rozklady.bielsko.pl/"

class BielskoVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): VehicleDataSource {
        val bielskoVehicleInterface =
            interfaceBuilder.create(BASE_URL, BielskoVehicleInterface::class)
        return BielskoVehicleDataSource(bielskoVehicleInterface)
    }
}