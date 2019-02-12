package com.kksionek.gdzietentramwaj.repository

import android.arch.lifecycle.LiveData
import com.kksionek.gdzietentramwaj.dataSource.TramData
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

internal class TramLiveData(
    private val mTramInterface: TramInterface,
    listConsumer: Consumer<Map<String, TramData>>
) : LiveData<TramDataWrapper>() {

    private val mDateFormat = SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss",
        Locale.getDefault()
    )
    private val mObservable: Flowable<TramDataWrapper>
    private var mDisposable: Disposable? = null

    init {
        mObservable = Flowable.interval(0, 10, TimeUnit.SECONDS)
            .subscribeOn(Schedulers.io())
            .flatMap { _ ->
                val cal = Calendar.getInstance()
                cal.add(Calendar.MINUTE, -2)
                val refDate = mDateFormat.format(cal.time)
                Observable.merge(
                    mTramInterface.trams()
                        .flatMapObservable { (list) -> Observable.fromIterable(list) },
                    mTramInterface.buses()
                        .flatMapObservable { (list) -> Observable.fromIterable(list) }
                )
                    .subscribeOn(Schedulers.io())
                    .filter { (time) -> refDate <= time }
                    .toMap { it.id }
                    .doOnSuccess(listConsumer)
                    .map { data -> TramDataWrapper.Success(data) as TramDataWrapper }
                    .onErrorReturn { TramDataWrapper.Error(it) }
                    .toFlowable()
                    .startWith(TramDataWrapper.InProgress)
            }
    }

    override fun onActive() {
        startLoading()
    }

    override fun onInactive() {
        mDisposable?.dispose()
    }

    fun forceReload() {
        if (hasActiveObservers()) {
            mDisposable?.dispose()
            startLoading()
        }
    }

    private fun startLoading() {
        mDisposable = mObservable.subscribe { this.postValue(it) }
    }
}
