package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single

class BielskoDifficultiesDataSource : DifficultiesDataSource {

    override fun isAvailable(): Boolean = false

    override fun getDifficulties(): Single<List<DifficultiesEntity>> = Single.never()
}