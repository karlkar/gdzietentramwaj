package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.krakow.TtssDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.lodz.LodzDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.ZtmDifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.MpkDifficultiesDataSourceFactory
import javax.inject.Inject

class DifficultiesDataSourceFactory @Inject constructor(
    private val ztmDifficultiesDataSourceFactory: ZtmDifficultiesDataSourceFactory,
    private val ttssDifficultiesDataSourceFactory: TtssDifficultiesDataSourceFactory,
    private val mpkDifficultiesDataSourceFactory: MpkDifficultiesDataSourceFactory,
    private val lodzDifficultiesDataSourceFactory: LodzDifficultiesDataSourceFactory
) {

    fun create(city: Cities): DifficultiesDataSource =
        when (city) {
            Cities.WARSAW -> ztmDifficultiesDataSourceFactory.create()
            Cities.KRAKOW -> ttssDifficultiesDataSourceFactory.create()
            Cities.WROCLAW -> mpkDifficultiesDataSourceFactory.create()
            Cities.LODZ -> lodzDifficultiesDataSourceFactory.create()
        }
}