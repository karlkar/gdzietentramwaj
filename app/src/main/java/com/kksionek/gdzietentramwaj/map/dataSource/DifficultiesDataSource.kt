package com.kksionek.gdzietentramwaj.map.dataSource

import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import io.reactivex.Single

interface DifficultiesDataSource {
    
    fun getDifficulties(): Single<DifficultiesState>
}