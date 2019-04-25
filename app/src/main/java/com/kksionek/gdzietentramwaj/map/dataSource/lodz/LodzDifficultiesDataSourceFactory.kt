package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class LodzDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {

    fun create(): DifficultiesDataSource = LodzDifficultiesDataSource()
}