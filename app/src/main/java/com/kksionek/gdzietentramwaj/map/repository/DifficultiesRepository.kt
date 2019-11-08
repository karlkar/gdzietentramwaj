package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DifficultiesRepository @Inject constructor(
    private val difficultiesDataSourceFactory: DifficultiesDataSourceFactory
) {
    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    private var selectedCity: Cities = Cities.WARSAW
    private lateinit var difficultiesDataSource: DifficultiesDataSource

    private fun selectCity(city: Cities) {
        if (selectedCity != city || !this::difficultiesDataSource.isInitialized) {
            selectedCity = city
            difficultiesDataSource = difficultiesDataSourceFactory.create(selectedCity)
        }
    }

    fun dataStream(city: Cities): Flowable<NetworkOperationResult<DifficultiesState>> {
        selectCity(city)
        return dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMapDelayError {
                Flowable.interval(0, 1, TimeUnit.MINUTES)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        difficultiesDataSource.getDifficulties()
                            .toNetworkOperationResult()
                            .toFlowable()
                            .startWith(NetworkOperationResult.InProgress())
                    }
            }
    }

    fun forceReload() {
        dataTrigger.onNext(Unit)
    }
}