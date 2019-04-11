package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single

class TtssDifficultiesDataSource: DifficultiesDataSource {
    override fun isAvailable(): Boolean = false

    override fun getDifficulties(): Single<List<DifficultiesEntity>> = Single.never()
}