package com.kksionek.gdzietentramwaj;

import android.support.annotation.UiThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.observable.ObservableInterval;
import io.reactivex.schedulers.Schedulers;

public class Model {

    private static final String TAG = "MODEL";

    private HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private final HashMap<String, TramData> mTmpTramDataHashMap = new HashMap<>();
    private FavoriteManager mFavoriteManager = null;
    private MapsActivity mMapsActivityObserver = null;
    private TramInterface mTramInterface = null;

    private Disposable mDisposable = null;
    private Disposable mDisposable2 = null;
    private Observable<Long> mObservable;

    private Model() {}

    public static Model getInstance() {
        return Holder.instance;
    }

    public FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    public void setObserver(MapsActivity observer, TramInterface tramInterface) {
        mMapsActivityObserver = observer;
        if (mFavoriteManager == null)
            mFavoriteManager = new FavoriteManager(observer);
        mTramInterface = tramInterface;
    }

    @UiThread
    public void startFetchingData() {
        Log.d(TAG, "startFetchingData: START");

        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();
        if (mDisposable2 != null && !mDisposable2.isDisposed())
            mDisposable2.dispose();

        Observable.interval(0, 30, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(Long value) {
                        Log.d(TAG, "onNext: NEW event from Interval");
                        if (mDisposable2 != null && !mDisposable2.isDisposed())
                            mDisposable2.dispose();

                        mTmpTramDataHashMap.clear();
                        mTramInterface.getTrams(TramInterface.ID, TramInterface.APIKEY)
                                .flatMap(tramList -> Observable.fromIterable(tramList.getList()))
                                .filter(tramData -> tramData.shouldBeVisible())
                                .map(tramData -> {
                                    mTmpTramDataHashMap.put(tramData.getId(), tramData);
                                    return tramData;
                                })
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .retryWhen(errors ->
                                        errors
                                                .zipWith(
                                                        Observable.range(1, 3), (n, i) -> i)
                                                .flatMap(
                                                        retryCount -> Observable.timer(5L * retryCount, TimeUnit.SECONDS)))
                                .subscribe(new Observer<TramData>() {
                                    @Override
                                    public void onSubscribe(Disposable d) {
                                        mDisposable2 = d;
                                    }

                                    @Override
                                    public void onNext(TramData value) {
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                    }

                                    @Override
                                    public void onComplete() {
                                        Log.d(TAG, "doOnComplete: ");
                                        synchronized (mTramDataHashMap) {
                                            mTramDataHashMap = mTmpTramDataHashMap;
                                        }
                                        notifyJobDone();
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void stopUpdates() {
        if (mDisposable != null && !mDisposable.isDisposed())
            mDisposable.dispose();

        if (mDisposable2 != null && !mDisposable2.isDisposed())
            mDisposable2.dispose();
    }

    public void notifyJobDone() {
        mMapsActivityObserver.notifyRefreshEnded();
        synchronized (mTramDataHashMap) {
            mMapsActivityObserver.updateMarkers(mTramDataHashMap);
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
