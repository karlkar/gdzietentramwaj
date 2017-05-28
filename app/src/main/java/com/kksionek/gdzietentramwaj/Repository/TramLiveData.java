package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;
import android.util.Pair;

import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

class TramLiveData extends LiveData<List<TramData>> {

    private static final String TAG = "TramLiveData";

    private final TramInterface mTramInterface;
    private final MutableLiveData<Boolean> mLoadingData = new MutableLiveData<>();
    private final Observable<List<TramData>> mObservable;
    private Disposable mDisposable;

    @Inject
    TramLiveData(TramInterface tramInterface) {
        mTramInterface = tramInterface;
        mObservable = Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(val -> {
                    mLoadingData.setValue(true);
                    return Observable.merge(
                            mTramInterface.getTrams(TramInterface.ID, TramInterface.APIKEY, TramInterface.TYPE_TRAM),
                            mTramInterface.getTrams(TramInterface.ID, TramInterface.APIKEY, TramInterface.TYPE_BUS)
                    ).subscribeOn(Schedulers.io())
                            .doOnNext(tramList -> Log.d(TAG, "getIntervalObservable: " + tramList.getList().size()))
                            .filter(tramList -> tramList.getList().size() > 0)
                            .flatMap(tramList -> Observable.fromIterable(tramList.getList()))
                            .toList()
                            .toObservable()
                            .retryWhen(errors -> errors.zipWith(
                                    Observable.just(1, 3, 5, 7),
                                    Pair::new)
                                    .flatMap(pair -> {
                                        if (pair.first instanceof UnknownHostException || pair.second > 5)
                                            return Observable.<Long>error(pair.first);
                                        return Observable.timer((long) pair.second, TimeUnit.SECONDS);
                                    }))
                            .observeOn(AndroidSchedulers.mainThread());
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
        mDisposable = mObservable.subscribe(tramData -> {
            mLoadingData.setValue(false);
            setValue(tramData);
        }, throwable -> {
            mLoadingData.setValue(false);
            throwable.printStackTrace();
        });
    }

    public LiveData<Boolean> getLoadingData() {
        return mLoadingData;
    }
}
