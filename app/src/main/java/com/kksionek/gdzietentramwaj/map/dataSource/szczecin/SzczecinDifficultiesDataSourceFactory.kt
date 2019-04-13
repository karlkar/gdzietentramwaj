package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

class SzczecinDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

    fun create(): DifficultiesDataSource = SzczecinDifficultiesDataSource()
}