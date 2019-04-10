package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.map.dataSource.krakow.TtssInterfaceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.ZtmInterfaceFactory
import javax.inject.Inject

class VehicleDataSourceFactory @Inject constructor(
    private val ztmInterfaceFactory: ZtmInterfaceFactory,
    private val ttssInterfaceFactory: TtssInterfaceFactory
) {

    fun create(city: Cities): VehicleDataSource =
        when (city) {
            Cities.WARSAW -> ztmInterfaceFactory.create()
            Cities.KRAKOW -> ttssInterfaceFactory.create()
        }
}