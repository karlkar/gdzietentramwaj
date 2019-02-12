package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.kksionek.gdzietentramwaj.dataSource.TramData;
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.dataSource.TramInterface;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class TramLiveData extends LiveData<TramDataWrapper> {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());

    private final TramInterface mTramInterface;
    private final Flowable<TramDataWrapper> mObservable;
    private Disposable mDisposable;

    TramLiveData(
            @NonNull TramInterface tramInterface,
            @NonNull Consumer<Map<String, TramData>> listConsumer) {
        mTramInterface = tramInterface;
        mObservable = Flowable.interval(0, 10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(val -> {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MINUTE, -2);
                    final String refDate = mDateFormat.format(cal.getTime());
                    return Observable.merge(
                            mTramInterface.trams()
                                    .flatMapObservable(tramList -> Observable.fromIterable(tramList.getList())),
                            mTramInterface.buses()
                                    .flatMapObservable(tramList -> Observable.fromIterable(tramList.getList()))
                    )
                            .subscribeOn(Schedulers.io())
                            .filter(tramData -> refDate.compareTo(tramData.getTime()) <= 0)
                            .toMap(TramData::getId, tramData -> tramData)
                            .doOnSuccess(listConsumer)
                            .map(data -> (TramDataWrapper)new TramDataWrapper.Success(data))
                            .onErrorReturn(TramDataWrapper.Error::new)
                            .toFlowable()
                            .startWith(TramDataWrapper.InProgress.INSTANCE);
                });
    }

    @Override
    protected void onActive() {
        startLoading();
    }

    @Override
    protected void onInactive() {
        mDisposable.dispose();
    }

    void forceReload() {
        if (hasActiveObservers()) {
            mDisposable.dispose();
            startLoading();
        }
    }

    private void startLoading() {
        mDisposable = mObservable.subscribe(this::postValue);
    }
}
