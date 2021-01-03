package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class LodzDifficultiesDataSourceFactory @Inject constructor() {

    fun create(): DifficultiesDataSource = LodzDifficultiesDataSource()
}