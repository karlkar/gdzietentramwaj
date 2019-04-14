package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.TtssDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.szczecin.SzczecinDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.WroclawDifficultiesDataSourceFactory
import javax.inject.Inject

class DifficultiesDataSourceFactory @Inject constructor(
    private val warsawDifficultiesDataSourceFactory: WarsawDifficultiesDataSourceFactory,
    private val ttssDifficultiesDataSourceFactory: TtssDifficultiesDataSourceFactory,
    private val wroclawDifficultiesDataSourceFactory: WroclawDifficultiesDataSourceFactory,
    private val lodzDifficultiesDataSourceFactory: LodzDifficultiesDataSourceFactory,
    private val szczecinDifficultiesDataSourceFactory: SzczecinDifficultiesDataSourceFactory
) {

    fun create(city: Cities): DifficultiesDataSource =
        when (city) {
            Cities.WARSAW -> warsawDifficultiesDataSourceFactory.create()
            Cities.KRAKOW -> ttssDifficultiesDataSourceFactory.create()
            Cities.WROCLAW -> wroclawDifficultiesDataSourceFactory.create()
            Cities.LODZ -> lodzDifficultiesDataSourceFactory.create()
            Cities.SZCZECIN -> szczecinDifficultiesDataSourceFactory.create()
        }
}