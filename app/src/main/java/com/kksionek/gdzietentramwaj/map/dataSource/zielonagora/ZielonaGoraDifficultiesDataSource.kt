package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single

class ZielonaGoraDifficultiesDataSource : DifficultiesDataSource {

    override fun isAvailable(): Boolean = false

    override fun getDifficulties(): Single<List<DifficultiesEntity>> = Single.never()
}