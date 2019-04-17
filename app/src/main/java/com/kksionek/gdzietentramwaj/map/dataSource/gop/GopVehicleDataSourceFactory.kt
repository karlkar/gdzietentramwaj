package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "http://sdip.metropoliaztm.pl"

class GopVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): VehicleDataSource {
        val gopVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(GopVehicleInterface::class.java)
        return GopVehicleDataSource(gopVehicleInterface)
    }
}