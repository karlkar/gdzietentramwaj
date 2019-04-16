package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "http://traveller.mzk.zgora.pl"

class ZielonaGoraVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): VehicleDataSource {
        val zielonaGoraVehicleInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(ZielonaGoraVehicleInterface::class.java)
        return ZielonaGoraVehicleDataSource(zielonaGoraVehicleInterface)
    }
}