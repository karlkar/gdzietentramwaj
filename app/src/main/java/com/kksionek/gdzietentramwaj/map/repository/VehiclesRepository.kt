package com.kksionek.gdzietentramwaj.map.repository

import androidx.annotation.CheckResult
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Flowable
import io.reactivex.Single

interface VehiclesRepository {

    @CheckResult
    fun dataStream(city: Cities): Flowable<NetworkOperationResult<List<VehicleData>>>

    @CheckResult
    fun getFavoriteVehicleLines(city: Cities): Single<List<String>>

    fun forceReload()
}