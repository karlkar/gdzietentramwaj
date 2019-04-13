package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.TtssVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.ZtmVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.MpkVehicleDataSourceFactory
import javax.inject.Inject

class VehicleDataSourceFactory @Inject constructor(
    private val ztmVehicleDataSourceFactory: ZtmVehicleDataSourceFactory,
    private val ttssVehicleDataSourceFactory: TtssVehicleDataSourceFactory,
    private val mpkVehicleDataSourceFactory: MpkVehicleDataSourceFactory,
    private val lodzVehicleDataSourceFactory: LodzVehicleDataSourceFactory
) {

    fun create(city: Cities): VehicleDataSource =
        when (city) {
            Cities.WARSAW -> ztmVehicleDataSourceFactory.create()
            Cities.KRAKOW -> ttssVehicleDataSourceFactory.create()
            Cities.WROCLAW -> mpkVehicleDataSourceFactory.create()
            Cities.LODZ -> lodzVehicleDataSourceFactory.create()
        }
}