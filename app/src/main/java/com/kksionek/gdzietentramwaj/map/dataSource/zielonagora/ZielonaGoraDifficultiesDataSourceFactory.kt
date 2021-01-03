package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class ZielonaGoraDifficultiesDataSourceFactory @Inject constructor() {

    fun create(): DifficultiesDataSource = ZielonaGoraDifficultiesDataSource()
}