package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class GopDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): DifficultiesDataSource = GopDifficultiesDataSource()
}