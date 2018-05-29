package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

class TramLiveData extends LiveData<TramDataWrapper> {

    private static final String TAG = "TramLiveData";

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
                .flatMap(val -> Single.concat(
                        Single.just(new TramDataWrapper(
                                null,
                                null,
                                true)),
                        Observable.merge(
                        mTramInterface.getTrams()
                                .flatMap(tramList -> Observable.fromIterable(tramList.getList())),
                        mTramInterface.getBuses()
                                .flatMap(tramList -> Observable.fromIterable(tramList.getList()))
                        ).filter(tramData -> {
                            try {
                                return (System.currentTimeMillis() - mDateFormat.parse(tramData.getTime()).getTime()) < 60000;
                            } catch (ParseException e) {
                                return false;
                            }
                        })
                        .toMap(TramData::getId, tramData -> tramData)
                        .doOnSuccess(listConsumer)
                        .map(tramData -> new TramDataWrapper(
                                tramData,
                                null,
                                false))
                        .onErrorResumeNext(throwable -> {
                            Log.e(TAG, "TramLiveData: Error occurred", throwable);
                            return Single.just(new TramDataWrapper(
                                    null,
                                    throwable,
                                    false));
                        })))
                .startWith(new TramDataWrapper(null, null, true));
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
