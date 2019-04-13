package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

const val BASE_URL = "http://rozklady.lodz.pl/"

class LodzVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): VehicleDataSource {
        val lodzVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(LodzVehicleInterface::class.java)
        return LodzVehicleDataSource(lodzVehicleInterface)
    }
}