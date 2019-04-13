package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import io.reactivex.Single

class LodzDifficultiesDataSource : DifficultiesDataSource {

    override fun isAvailable(): Boolean = false //TODO Check if it can be implemented

    override fun getDifficulties(): Single<List<DifficultiesEntity>> = Single.never()
}