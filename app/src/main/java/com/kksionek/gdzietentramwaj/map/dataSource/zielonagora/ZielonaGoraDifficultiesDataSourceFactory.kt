package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class ZielonaGoraDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): DifficultiesDataSource = ZielonaGoraDifficultiesDataSource()
}