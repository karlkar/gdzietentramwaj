package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import io.reactivex.Single

class BielskoDifficultiesDataSource : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        Single.just(DifficultiesState(false, emptyList()))
}