package com.kksionek.gdzietentramwaj.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import com.kksionek.gdzietentramwaj.data.FavoriteTramData;
import com.kksionek.gdzietentramwaj.data.TramData;
import com.kksionek.gdzietentramwaj.data.TramInterface;
import com.kksionek.gdzietentramwaj.view.ModelObserverInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Model {

    private static final String TAG = "MODEL";
    private final HashMap<String, TramData> mTmpTramDataHashMap = new HashMap<>();
    private HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private FavoriteManager mFavoriteManager = null;
    private WeakReference<ModelObserverInterface> mModelObserver = null;
    private TramInterface mTramInterface = null;

    private Disposable mDisposableInterval = null;
    private Disposable mDisposableRetrofit = null;
    private Observable<Long> mIntervalObservable = null;
    private Observable<TramData> mRetrofitObservable = null;

    private Model() {
    }

    public static Model getInstance() {
        return Holder.instance;
    }

    public FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    public void setObserver(@NonNull ModelObserverInterface observer, @NonNull Context ctx, @NonNull TramInterface tramInterface) {
        mModelObserver = new WeakReference<>(observer);
        mFavoriteManager = new FavoriteManager(ctx);
        mTramInterface = tramInterface;
        mIntervalObservable = getIntervalObservable();
        mRetrofitObservable = getRetrofitObservable();
    }

    @UiThread
    public void startFetchingData() {
        Log.d(TAG, "startFetchingData: START");

        if (mDisposableInterval != null && !mDisposableInterval.isDisposed())
            mDisposableInterval.dispose();
        if (mDisposableRetrofit != null && !mDisposableRetrofit.isDisposed())
            mDisposableRetrofit.dispose();

        mIntervalObservable.subscribe();
    }

    private Observable<Long> getIntervalObservable() {
        return Observable.interval(0, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSubscribe(disposable -> mDisposableInterval = disposable)
                .doOnError(throwable -> Log.d(TAG, "onError: IntervalObservable - " + throwable.getMessage()))
                .doOnNext(l -> {
                    Log.d(TAG, "onNext: NEW event from Interval");
                    if (mDisposableRetrofit != null && !mDisposableRetrofit.isDisposed())
                        mDisposableRetrofit.dispose();

                    mTmpTramDataHashMap.clear();
                    mRetrofitObservable.subscribe();
                });
    }

    private Observable<TramData> getRetrofitObservable() {
        return mTramInterface.getTrams(TramInterface.ID, TramInterface.APIKEY)
                .subscribeOn(Schedulers.io())
                .flatMap(tramList -> Observable.fromIterable(tramList.getList())
                        .filter(tramData -> tramData.shouldBeVisible()))
                        .doOnNext(TramData::trimStrings)
                .doOnNext(tramData -> mTmpTramDataHashMap.put(tramData.getId(), tramData))
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(errors ->
                        errors
                                .zipWith(
                                        Observable.range(1, 3), (n, i) -> i)
                                .flatMap(
                                        retryCount -> Observable.timer(5L * retryCount, TimeUnit.SECONDS)))
                .doOnSubscribe(disposable -> mDisposableRetrofit = disposable)
                .doOnComplete(() -> {
                    Log.d(TAG, "doOnComplete: ");
                    synchronized (mTramDataHashMap) {
                        mTramDataHashMap = mTmpTramDataHashMap;
                    }
                    notifyJobDone();
                });
    }

    public void stopUpdates() {
        if (mDisposableInterval != null && !mDisposableInterval.isDisposed())
            mDisposableInterval.dispose();

        if (mDisposableRetrofit != null && !mDisposableRetrofit.isDisposed())
            mDisposableRetrofit.dispose();
    }

    public void notifyJobDone() {
        ModelObserverInterface mapsActivity = mModelObserver.get();
        if (mapsActivity != null) {
            mapsActivity.notifyRefreshEnded();
            synchronized (mTramDataHashMap) {
                mapsActivity.updateMarkers(mTramDataHashMap);
            }
        }
    }

    public List<FavoriteTramData> getFavoriteTramData() {
        SortedSet<FavoriteTramData> favoriteTrams = new TreeSet<>();
        for (String str : mFavoriteManager.getFavoriteTramData())
            favoriteTrams.add(new FavoriteTramData(str, true));

        synchronized (mTramDataHashMap) {
            for (TramData tramData : mTramDataHashMap.values())
                if (!mFavoriteManager.isFavorite(tramData.getFirstLine()))
                    favoriteTrams.add(new FavoriteTramData(tramData.getFirstLine(), false));
        }
        return new ArrayList<>(favoriteTrams);
    }

    private static class Holder {
        static final Model instance = new Model();
    }
}
