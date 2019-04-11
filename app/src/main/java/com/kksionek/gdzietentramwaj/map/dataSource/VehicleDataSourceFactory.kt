package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.map.dataSource.krakow.TtssVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.ZtmVehicleDataSourceFactory
import javax.inject.Inject

class VehicleDataSourceFactory @Inject constructor(
    private val ztmVehicleDataSourceFactory: ZtmVehicleDataSourceFactory,
    private val ttssVehicleDataSourceFactory: TtssVehicleDataSourceFactory
) {

    fun create(city: Cities): VehicleDataSource =
        when (city) {
            Cities.WARSAW -> ztmVehicleDataSourceFactory.create()
            Cities.KRAKOW -> ttssVehicleDataSourceFactory.create()
        }
}