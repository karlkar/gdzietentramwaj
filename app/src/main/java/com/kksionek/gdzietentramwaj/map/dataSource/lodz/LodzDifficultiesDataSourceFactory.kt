package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class LodzDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): DifficultiesDataSource = LodzDifficultiesDataSource()
}