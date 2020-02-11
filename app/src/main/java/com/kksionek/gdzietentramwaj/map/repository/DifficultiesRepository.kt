package com.kksionek.gdzietentramwaj.map.repository

import androidx.annotation.CheckResult
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import io.reactivex.Flowable

interface DifficultiesRepository {

    @CheckResult
    fun dataStream(city: Cities): Flowable<NetworkOperationResult<DifficultiesState>>

    fun forceReload()
}