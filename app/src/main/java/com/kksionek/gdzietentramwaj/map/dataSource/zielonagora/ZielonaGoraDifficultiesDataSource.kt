package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import io.reactivex.Single

class ZielonaGoraDifficultiesDataSource : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        Single.just(DifficultiesState(false, emptyList())) // TODO Check if can be implemented
}