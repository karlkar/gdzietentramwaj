package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

private const val MAX_RETRIES = 3

class VehiclesRepositoryImpl constructor(
    private val tramDao: TramDao,
    private val vehicleDataSourceFactory: VehicleDataSourceFactory
) : VehiclesRepository {
    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    private var selectedCity: Cities = Cities.WARSAW
    private lateinit var vehicleDataSource: VehicleDataSource
    private lateinit var favoriteRepositoryAdder: FavoriteLinesConsumer

    override fun dataStream(city: Cities): Flowable<NetworkOperationResult<List<VehicleData>>> {
        if (selectedCity != city || !this::vehicleDataSource.isInitialized) {
            selectedCity = city
            vehicleDataSource = vehicleDataSourceFactory.create(selectedCity)
            favoriteRepositoryAdder = FavoriteLinesConsumer(tramDao, selectedCity)
        }

        val vehiclesStream = vehicleDataSource.vehicles()
            .subscribeOn(Schedulers.io())
            .retryWhen { errors ->
                errors.zipWith(
                    Flowable.range(1, MAX_RETRIES + 1),
                    { t: Throwable, i: Int -> i to t }
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

        return dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMapDelayError {
                Flowable.interval(0, 10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap { vehiclesStream }
            }
    }

    override fun getFavoriteVehicleLines(city: Cities): Single<List<String>> =
        tramDao.getAllVehicles(city.id)
            .distinctUntilChanged()
            .map { list -> list.filter { it.isFavorite } }
            .map { list -> list.map { it.lineId } }
            .first(emptyList())

    override fun forceReload() {
        dataTrigger.onNext(Unit)
    }
}
