package com.kksionek.gdzietentramwaj.base.repository

import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.dataSource.TramData
import com.kksionek.gdzietentramwaj.map.dataSource.TramInterface
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TramRepository @Inject constructor(
    private val mTramDao: TramDao,
    private val tramInterface: TramInterface,
    private val favoriteRepositoryAdder: FavoriteLinesConsumer
) {
    //TODO divide it into map and favorite parts
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
                    }
            }

    private fun <T> Single<T>.toNetworkOperationResult() =
        map { data -> NetworkOperationResult.Success(data) as NetworkOperationResult<T> }
            .onErrorReturn { NetworkOperationResult.Error(it) }

    val allFavTrams: Flowable<List<FavoriteTram>>
        get() = mTramDao.getAllFavTrams()

    val favoriteTrams: Flowable<List<String>>
        get() = mTramDao.getFavoriteTrams()

    fun forceReload() {
        dataTrigger.onNext(Unit)
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        mTramDao.setFavorite(lineId, favorite)
    }
}
