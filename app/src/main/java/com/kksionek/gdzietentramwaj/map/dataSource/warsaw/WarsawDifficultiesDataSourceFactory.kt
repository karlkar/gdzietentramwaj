package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import javax.inject.Inject

private const val BASE_URL = "https://www.wtp.waw.pl/"

class WarsawDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder,
    private val xmlDeserializer: XmlDeserializer
) {
    fun create(): DifficultiesDataSource {
        val ztmDifficultiesInterface =
            interfaceBuilder.create(BASE_URL, WarsawDifficultiesInterface::class)
        return WarsawDifficultiesDataSource(ztmDifficultiesInterface, xmlDeserializer)
    }
}