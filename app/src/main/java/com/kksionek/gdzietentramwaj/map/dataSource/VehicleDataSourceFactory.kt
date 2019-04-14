package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.KrakowVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.szczecin.SzczecinVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.WroclawVehicleDataSourceFactory
import javax.inject.Inject

class VehicleDataSourceFactory @Inject constructor(
    private val warsawVehicleDataSourceFactory: WarsawVehicleDataSourceFactory,
    private val krakowVehicleDataSourceFactory: KrakowVehicleDataSourceFactory,
    private val wroclawVehicleDataSourceFactory: WroclawVehicleDataSourceFactory,
    private val lodzVehicleDataSourceFactory: LodzVehicleDataSourceFactory,
    private val szczecinVehicleDataSourceFactory: SzczecinVehicleDataSourceFactory
) {

    fun create(city: Cities): VehicleDataSource =
        when (city) {
            Cities.WARSAW -> warsawVehicleDataSourceFactory.create()
            Cities.KRAKOW -> krakowVehicleDataSourceFactory.create()
            Cities.WROCLAW -> wroclawVehicleDataSourceFactory.create()
            Cities.LODZ -> lodzVehicleDataSourceFactory.create()
            Cities.SZCZECIN -> szczecinVehicleDataSourceFactory.create()
        }
}