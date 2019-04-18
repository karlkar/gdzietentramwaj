package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import io.reactivex.Single

class SzczecinDifficultiesDataSource : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        Single.just(DifficultiesState(false, emptyList()))
}