package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "https://api.um.warszawa.pl/"

class ZtmInterfaceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): VehicleDataSource {
        val ztmVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(ZtmVehicleInterface::class.java)
        return VehicleZtmInterface(ztmVehicleInterface)
    }
}