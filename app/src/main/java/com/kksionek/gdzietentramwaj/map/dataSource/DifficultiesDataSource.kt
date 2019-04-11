package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single

interface DifficultiesDataSource {

    fun isAvailable(): Boolean

    fun getDifficulties(): Single<List<DifficultiesEntity>>
}