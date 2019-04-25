package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val BASE_URL = "http://traveller.mzk.zgora.pl"

class ZielonaGoraVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): VehicleDataSource {
        val zielonaGoraVehicleInterface =
            interfaceBuilder.create(BASE_URL, ZielonaGoraVehicleInterface::class)
        return ZielonaGoraVehicleDataSource(zielonaGoraVehicleInterface)
    }
}