package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val TRAM_BASE_URL = "http://www.ttss.krakow.pl/"
private const val BUS_BASE_URL = "http://91.223.13.70/"

class TtssVehicleDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): VehicleDataSource {
        val ttssTramInterface = retrofitBuilder
            .baseUrl(TRAM_BASE_URL)
            .build()
            .create(TtssTramInterface::class.java)
        val ttssBusInterface = retrofitBuilder
            .baseUrl(BUS_BASE_URL)
            .build()
            .create(TtssBusInterface::class.java)
        return TtssVehicleDataSource(ttssTramInterface, ttssBusInterface)
    }
}