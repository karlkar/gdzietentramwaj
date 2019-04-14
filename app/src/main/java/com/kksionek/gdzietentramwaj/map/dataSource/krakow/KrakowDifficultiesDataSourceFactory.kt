package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "http://mpk.krakow.pl"

class KrakowDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): DifficultiesDataSource {
        val ttssDifficultiesInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(KrakowDifficultiesInterface::class.java)
        return KrakowDifficultiesDataSource(ttssDifficultiesInterface)
    }
}