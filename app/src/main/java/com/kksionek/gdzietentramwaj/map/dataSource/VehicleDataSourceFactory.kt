package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.bielsko.BielskoVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.gop.GopVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.KrakowVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.szczecin.SzczecinVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.WroclawVehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.zielonagora.ZielonaGoraVehicleDataSourceFactory
import javax.inject.Inject

class VehicleDataSourceFactory @Inject constructor(
    private val warsawVehicleDataSourceFactory: WarsawVehicleDataSourceFactory,
    private val krakowVehicleDataSourceFactory: KrakowVehicleDataSourceFactory,
    private val wroclawVehicleDataSourceFactory: WroclawVehicleDataSourceFactory,
    private val lodzVehicleDataSourceFactory: LodzVehicleDataSourceFactory,
    private val szczecinVehicleDataSourceFactory: SzczecinVehicleDataSourceFactory,
    private val bielskoVehicleDataSourceFactory: BielskoVehicleDataSourceFactory,
    private val zielonaGoraVehicleDataSourceFactory: ZielonaGoraVehicleDataSourceFactory,
    private val gopVehicleDataSourceFactory: GopVehicleDataSourceFactory
    // TODO Add Rzeszow?
) {

    fun create(city: Cities): VehicleDataSource =
        when (city) {
            Cities.WARSAW -> warsawVehicleDataSourceFactory.create()
            Cities.KRAKOW -> krakowVehicleDataSourceFactory.create()
            Cities.WROCLAW -> wroclawVehicleDataSourceFactory.create()
            Cities.LODZ -> lodzVehicleDataSourceFactory.create()
            Cities.SZCZECIN -> szczecinVehicleDataSourceFactory.create()
            Cities.BIELSKO -> bielskoVehicleDataSourceFactory.create()
            Cities.ZIELONA -> zielonaGoraVehicleDataSourceFactory.create()
            Cities.GOP -> gopVehicleDataSourceFactory.create()
        }
}