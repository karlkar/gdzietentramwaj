package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val BASE_URL = "http://sdip.metropoliaztm.pl"

class GopVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): VehicleDataSource {
        val gopVehicleInterface = interfaceBuilder.create(BASE_URL, GopVehicleInterface::class)
        return GopVehicleDataSource(gopVehicleInterface)
    }
}