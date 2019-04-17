package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.bielsko.BielskoDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.gop.GopDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.KrakowDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.szczecin.SzczecinDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.WroclawDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.zielonagora.ZielonaGoraDifficultiesDataSourceFactory
import javax.inject.Inject

class DifficultiesDataSourceFactory @Inject constructor(
    private val warsawDifficultiesDataSourceFactory: WarsawDifficultiesDataSourceFactory,
    private val krakowDifficultiesDataSourceFactory: KrakowDifficultiesDataSourceFactory,
    private val wroclawDifficultiesDataSourceFactory: WroclawDifficultiesDataSourceFactory,
    private val lodzDifficultiesDataSourceFactory: LodzDifficultiesDataSourceFactory,
    private val szczecinDifficultiesDataSourceFactory: SzczecinDifficultiesDataSourceFactory,
    private val bielskoDifficultiesDataSourceFactory: BielskoDifficultiesDataSourceFactory,
    private val zielonaGoraDifficultiesDataSourceFactory: ZielonaGoraDifficultiesDataSourceFactory,
    private val gopDifficultiesDataSourceFactory: GopDifficultiesDataSourceFactory
) {

    fun create(city: Cities): DifficultiesDataSource =
        when (city) {
            Cities.WARSAW -> warsawDifficultiesDataSourceFactory.create()
            Cities.KRAKOW -> krakowDifficultiesDataSourceFactory.create()
            Cities.WROCLAW -> wroclawDifficultiesDataSourceFactory.create()
            Cities.LODZ -> lodzDifficultiesDataSourceFactory.create()
            Cities.SZCZECIN -> szczecinDifficultiesDataSourceFactory.create()
            Cities.BIELSKO -> bielskoDifficultiesDataSourceFactory.create()
            Cities.ZIELONA -> zielonaGoraDifficultiesDataSourceFactory.create()
            Cities.GOP -> gopDifficultiesDataSourceFactory.create()
        }
}