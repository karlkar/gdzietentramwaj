package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "https://rozklady.bielsko.pl/"

class BielskoVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): VehicleDataSource {
        val bielskoVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(BielskoVehicleInterface::class.java)
        return BielskoVehicleDataSource(bielskoVehicleInterface)
    }
}