package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single

class GopDifficultiesDataSource: DifficultiesDataSource {

    override fun isAvailable(): Boolean = false // TODO Check

    override fun getDifficulties(): Single<List<DifficultiesEntity>> = Single.never()
}