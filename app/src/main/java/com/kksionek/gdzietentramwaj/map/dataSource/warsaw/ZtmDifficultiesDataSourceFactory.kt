package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "https://www.ztm.waw.pl/"

class ZtmDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): DifficultiesDataSource {
        val ztmDifficultiesInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(ZtmDifficultiesInterface::class.java)
        return ZtmDifficultiesDataSource(ztmDifficultiesInterface)
    }
}