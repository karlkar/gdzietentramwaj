package com.kksionek.gdzietentramwaj.repository

import android.arch.lifecycle.LiveData

import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

import javax.inject.Inject

class TramRepository @Inject constructor(
    private val mTramDao: TramDao,
    private val tramInterface: TramInterface
) {
    private val dateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )
    private val favoriteRepositoryAdder: FavoriteLinesConsumer = FavoriteLinesConsumer(mTramDao)

    private val dataTrigger: PublishSubject<Unit> = PublishSubject.create()

    val dataStream: Flowable<TramDataWrapper> =
        dataTrigger.toFlowable(BackpressureStrategy.DROP)
            .startWith(Unit)
            .switchMap {
                Flowable.interval(0, 10, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .flatMap {
                        val cal = Calendar.getInstance()
                        cal.add(Calendar.MINUTE, -2)
                        val refDate = dateFormat.format(cal.time)
                        Observable.merge(
                            tramInterface.trams()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) },
                            tramInterface.buses()
                                .flatMapObservable { (list) -> Observable.fromIterable(list) }
                        )
                            .subscribeOn(Schedulers.io())
                            .filter { (time) -> refDate <= time }
                            .toMap { it.id }
                            .doOnSuccess(favoriteRepositoryAdder)
                            .map { data -> TramDataWrapper.Success(data) as TramDataWrapper }
                            .onErrorReturn { TramDataWrapper.Error(it) }
                            .toFlowable()
                    }
                    .startWith(TramDataWrapper.InProgress)
            }

    val allFavTrams: LiveData<List<FavoriteTram>>
        get() = mTramDao.getAllFavTrams()

    val favoriteTrams: LiveData<List<String>>
        get() = mTramDao.getFavoriteTrams()

    fun forceReload() {
        dataTrigger.onNext(Unit)
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        mTramDao.setFavorite(lineId, favorite)
    }
}
