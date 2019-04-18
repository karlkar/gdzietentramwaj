package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import io.reactivex.Single

class GopDifficultiesDataSource : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> =
        Single.just(DifficultiesState(false, emptyList())) // TODO Check
}