package com.kksionek.gdzietentramwaj.repository

import com.kksionek.gdzietentramwaj.dataSource.NetworkOperationResult
import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TramRepository @Inject constructor(
    private val mTramDao: TramDao,
    private val tramInterface: TramInterface,
    private val favoriteRepositoryAdder: FavoriteLinesConsumer
) {
    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )
    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    val dataStream: Flowable<NetworkOperationResult<List<TramData>>> =
        dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMap {
                Flowable.interval(0, 10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.MINUTE, -2)
                        val refDate = dateFormat.format(cal.time)
                        Observable.mergeDelayError(
                            tramInterface.trams()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) },
                            tramInterface.buses()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) }
                        )
                            .subscribeOn(Schedulers.io())
                            .filter { (time) -> refDate <= time }
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
