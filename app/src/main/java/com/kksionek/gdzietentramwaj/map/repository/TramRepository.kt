package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.dataSource.TramData
import com.kksionek.gdzietentramwaj.map.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.toNetworkOperationResult
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TramRepository @Inject constructor(
    private val tramDao: TramDao,
    private val tramInterface: TramInterface,
    private val favoriteRepositoryAdder: FavoriteLinesConsumer
) {
    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    val dataStream: Flowable<NetworkOperationResult<List<TramData>>> =
        dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMap {
                Flowable.interval(0, 10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        Observable.mergeDelayError(
                            tramInterface.trams()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) },
                            tramInterface.buses()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) }
                        )
                            .subscribeOn(Schedulers.io())
                            .toList()
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
}
