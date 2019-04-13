package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single

interface DifficultiesDataSource {

    fun isAvailable(): Boolean // TODO Should not exist. getDifficulties should return one element

    fun getDifficulties(): Single<List<DifficultiesEntity>>
}