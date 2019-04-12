package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class MpkDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): DifficultiesDataSource = MpkDifficultiesDataSource()
}