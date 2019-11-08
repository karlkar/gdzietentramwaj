package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import io.reactivex.Single

class LodzDifficultiesDataSource : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        Single.just(DifficultiesState(false, emptyList())) // Can be done, but MPK must agree
}