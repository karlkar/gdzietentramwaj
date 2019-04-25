package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

private const val BASE_URL = "http://mpk.krakow.pl"

class KrakowDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder
) {
    fun create(): DifficultiesDataSource {
        val ttssDifficultiesInterface =
            interfaceBuilder.create(BASE_URL, KrakowDifficultiesInterface::class)
        return KrakowDifficultiesDataSource(ttssDifficultiesInterface)
    }
}