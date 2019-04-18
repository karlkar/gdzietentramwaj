package com.kksionek.gdzietentramwaj.map.dataSource

import io.reactivex.Single

interface DifficultiesDataSource {
    
    fun getDifficulties(): Single<DifficultiesState>
}