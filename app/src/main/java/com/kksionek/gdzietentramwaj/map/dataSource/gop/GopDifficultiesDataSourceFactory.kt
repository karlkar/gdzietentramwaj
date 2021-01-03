package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class GopDifficultiesDataSourceFactory @Inject constructor() {

    fun create(): DifficultiesDataSource = GopDifficultiesDataSource()
}