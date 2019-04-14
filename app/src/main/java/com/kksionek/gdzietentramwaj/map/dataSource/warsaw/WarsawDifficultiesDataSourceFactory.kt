package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "https://www.ztm.waw.pl/"

class WarsawDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): DifficultiesDataSource {
        val ztmDifficultiesInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(WarsawDifficultiesInterface::class.java)
        return WarsawDifficultiesDataSource(ztmDifficultiesInterface)
    }
}