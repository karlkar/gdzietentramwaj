package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class TtssDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {
    fun create(): DifficultiesDataSource = TtssDifficultiesDataSource()
}