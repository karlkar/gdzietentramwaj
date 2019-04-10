package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val MAX_RETRIES = 3

class TramRepository @Inject constructor(
    private val tramDao: TramDao,
    private val vehicleDataSourceFactory: VehicleDataSourceFactory,
    private val favoriteRepositoryAdder: FavoriteLinesConsumer
) {
    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    private var selectedCity = Cities.KRAKOW
    private var vehicleDataSource: VehicleDataSource = vehicleDataSourceFactory.create(selectedCity)

    val dataStream: Flowable<NetworkOperationResult<List<VehicleData>>> =
        dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMapDelayError {
                Flowable.interval(0, 10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        Observable.mergeDelayError(
                            vehicleDataSource.trams()
                                .flatMapObservable { list -> Observable.fromIterable(list) },
                            vehicleDataSource.buses()
                                .flatMapObservable { list -> Observable.fromIterable(list) }
                        )
                            .subscribeOn(Schedulers.io())
                            .toList()
                            .retryWhen { errors ->
                                errors.zipWith(
                                    Flowable.range(1, MAX_RETRIES + 1),
                                    BiFunction { t: Throwable, i: Int -> i to t }
                                )
                                    .flatMap { (retry, error) ->
                                        if (retry < MAX_RETRIES) {
                                            Flowable.timer(retry * 100L, TimeUnit.MILLISECONDS)
                                        } else {
                                            Flowable.error(error)
                                        }
                                    }
                            }
                            .doOnSuccess(favoriteRepositoryAdder)
                            .toNetworkOperationResult()
                            .toFlowable()
                            .startWith(NetworkOperationResult.InProgress())
                    }
            }

    val favoriteTrams: Flowable<List<String>>
        get() = tramDao.getFavoriteTrams()

    fun forceReload() {
        dataTrigger.onNext(Unit)
    }

    fun changeCity(city: Cities) {
        if (selectedCity == city) return
        selectedCity = city
        vehicleDataSource = vehicleDataSourceFactory.create(selectedCity)
    }
}
