package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class BielskoDifficultiesDataSourceFactory @Inject constructor() {

    fun create(): DifficultiesDataSource = BielskoDifficultiesDataSource()
}