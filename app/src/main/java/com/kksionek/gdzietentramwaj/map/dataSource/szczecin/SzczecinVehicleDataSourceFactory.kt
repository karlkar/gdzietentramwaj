package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import javax.inject.Inject

const val BASE_URL = "https://www.zditm.szczecin.pl/"

class SzczecinVehicleDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): VehicleDataSource {
        val szczecinVehicleInterface =
            interfaceBuilder.create(BASE_URL, SzczecinVehicleInterface::class)
        return SzczecinVehicleDataSource(szczecinVehicleInterface)
    }
}