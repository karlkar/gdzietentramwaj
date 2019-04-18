package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.Observable
import javax.inject.Inject

class DifficultiesRepository @Inject constructor(
    private val difficultiesDataSourceFactory: DifficultiesDataSourceFactory
) {

    private var selectedCity: Cities = Cities.WARSAW
    private lateinit var difficultiesDataSource: DifficultiesDataSource

    private fun selectCity(city: Cities) {
        if (selectedCity != city || !this::difficultiesDataSource.isInitialized) {
            selectedCity = city
            difficultiesDataSource = difficultiesDataSourceFactory.create(selectedCity)
        }
    }

    fun getDifficulties(city: Cities): Observable<NetworkOperationResult<DifficultiesState>> {
        selectCity(city)
        return difficultiesDataSource.getDifficulties()
            .toNetworkOperationResult()
            .toObservable()
            .startWith(NetworkOperationResult.InProgress())
    }
}