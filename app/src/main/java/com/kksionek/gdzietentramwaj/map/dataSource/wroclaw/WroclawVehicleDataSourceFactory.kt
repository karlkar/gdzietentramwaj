package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "https://www.wroclaw.pl"

class WroclawVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): VehicleDataSource {
        val mpkVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(WroclawVehicleInterface::class.java)
        return WroclawVehicleDataSource(mpkVehicleInterface)
    }
}