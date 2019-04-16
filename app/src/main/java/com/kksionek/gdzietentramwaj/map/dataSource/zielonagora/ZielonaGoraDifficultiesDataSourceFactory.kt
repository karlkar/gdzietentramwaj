package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class ZielonaGoraDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): DifficultiesDataSource = ZielonaGoraDifficultiesDataSource()
}