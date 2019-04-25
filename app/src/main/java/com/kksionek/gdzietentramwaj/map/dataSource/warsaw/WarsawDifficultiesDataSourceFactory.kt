package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

private const val BASE_URL = "https://www.ztm.waw.pl/"

class WarsawDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {
    fun create(): DifficultiesDataSource {
        val ztmDifficultiesInterface =
            interfaceBuilder.create(BASE_URL, WarsawDifficultiesInterface::class)
        return WarsawDifficultiesDataSource(ztmDifficultiesInterface)
    }
}