package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

private const val TRAM_BASE_URL = "http://www.ttss.krakow.pl/"
private const val BUS_BASE_URL = "http://91.223.13.70/"

class KrakowVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {
    fun create(): VehicleDataSource {
        val ttssTramInterface = interfaceBuilder.create(TRAM_BASE_URL, KrakowTramInterface::class)
        val ttssBusInterface = interfaceBuilder.create(BUS_BASE_URL, KrakowBusInterface::class)
        return KrakowVehicleDataSource(ttssTramInterface, ttssBusInterface)
    }
}