package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val BASE_URL = "https://api.um.warszawa.pl/"

class WarsawVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder,
    private val warsawApikeyRepository: WarsawApikeyRepository
) {
    fun create(): VehicleDataSource {
        val ztmVehicleInterface = interfaceBuilder.create(BASE_URL, WarsawVehicleInterface::class)
        return WarsawVehicleDataSource(ztmVehicleInterface, warsawApikeyRepository)
    }
}