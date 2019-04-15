package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class BielskoDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): DifficultiesDataSource = BielskoDifficultiesDataSource()
}