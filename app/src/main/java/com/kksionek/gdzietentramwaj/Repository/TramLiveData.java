package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

class TramLiveData extends LiveData<TramDataWrapper> {

    private static final String TAG = "TramLiveData";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());

    private final TramInterface mTramInterface;
    private final MutableLiveData<Boolean> mLoadingData = new MutableLiveData<>();
    private final Observable<TramDataWrapper> mObservable;
    private Disposable mDisposable;

    TramLiveData(
            @NonNull TramInterface tramInterface,
            @NonNull Function<List<TramData>, ObservableSource<List<TramData>>> listConsumer) {
        mTramInterface = tramInterface;
        mObservable = Observable.interval(0, 10, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .flatMap(val -> {
                    mLoadingData.postValue(true);
                    return Observable.zip(
                            mTramInterface.getTrams(),
                            mTramInterface.getBuses(),
                            (tramList, busList) -> {
                                List<TramData> tramDataList = tramList.getList();
                                tramDataList.addAll(busList.getList());
                                return tramDataList;
                            }
                    )
                    .doOnNext(tramList -> Log.d(TAG, "getIntervalObservable: " + tramList.size()))
                    .flatMap(list -> {
                        long currentTime = System.currentTimeMillis();
                        return Observable.fromIterable(list)
                                .filter(tramData -> {
                                    try {
                                        return (currentTime - mDateFormat.parse(tramData.getTime()).getTime()) < 60000;
                                    } catch (ParseException e) {
                                        return false;
                                    }
                                })
                                .toList()
                                .toObservable();
                    })
                    .flatMap(listConsumer)
                    .map(tramData -> new TramDataWrapper(tramData, null))
                    .onErrorResumeNext(throwable -> {
                        Log.e(TAG, "TramLiveData: Error occurred", throwable);
                        return Observable.just(new TramDataWrapper(null, throwable));
                    });
                })
                .observeOn(AndroidSchedulers.mainThread());
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
        mDisposable = mObservable.subscribe(tramData -> {
            mLoadingData.setValue(false);
            setValue(tramData);
        });
    }

    public LiveData<Boolean> getLoadingData() {
        return mLoadingData;
    }
}
