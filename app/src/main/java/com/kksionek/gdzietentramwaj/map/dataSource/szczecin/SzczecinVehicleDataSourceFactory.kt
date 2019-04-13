package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

const val BASE_URL = "https://www.zditm.szczecin.pl/"

class SzczecinVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): VehicleDataSource {
        val szczecinVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(SzczecinVehicleInterface::class.java)
        return SzczecinVehicleDataSource(szczecinVehicleInterface)
    }
}