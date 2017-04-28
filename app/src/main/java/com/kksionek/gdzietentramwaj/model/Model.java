package com.kksionek.gdzietentramwaj.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.util.Pair;

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
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Model {

    private static final String TAG = "MODEL";
    private final HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private FavoriteManager mFavoriteManager = null;
    private WeakReference<ModelObserverInterface> mModelObserver = null;
    private TramInterface mTramInterface = null;

    private Disposable mDisposableRetrofit = null;
    private Observable<List<TramData>> mRetrofitObservable = null;

    private Model() {
    }

    public static Model getInstance() {
        return Holder.instance;
    }

    public FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    public void setObserver(
            @NonNull ModelObserverInterface observer,
            @NonNull Context ctx,
            @NonNull TramInterface tramInterface) {
        mModelObserver = new WeakReference<>(observer);
        mFavoriteManager = new FavoriteManager(ctx);
        mTramInterface = tramInterface;
        mRetrofitObservable = getIntervalObservable();
    }

    @UiThread
    public void startFetchingData() {
        Log.d(TAG, "startFetchingData: START");

        stopUpdates();

        mDisposableRetrofit = mRetrofitObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        tramDatas -> {
                            Log.d(TAG, "startFetchingData: onNext");
                            notifyJobDone();
                        },
                        throwable -> {
                            Log.d(TAG, "startFetchingData: onError");
                            ModelObserverInterface observer = mModelObserver.get();
                            if (observer != null) {
                                observer.notifyRefreshEnded();
                            }
                            throwable.printStackTrace();
                        },
                        () -> Log.d(TAG, "startFetchingData: onComplete"));
    }

    private Observable<List<TramData>> getIntervalObservable() {
        return Observable.interval(0, 10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(val -> {
                    synchronized (mTramDataHashMap) {
                        mTramDataHashMap.clear();
                    }
                    mModelObserver.get().notifyRefreshStarted();
                    return Observable.merge(
                            mTramInterface.getTrams(
                                    TramInterface.ID,
                                    TramInterface.APIKEY,
                                    TramInterface.TYPE_BUS),
                            mTramInterface.getTrams(
                                    TramInterface.ID,
                                    TramInterface.APIKEY,
                                    TramInterface.TYPE_TRAM))
                            .subscribeOn(Schedulers.io())
                            .doOnNext(tramList -> Log.d(TAG, "getIntervalObservable: " + tramList.getList().size()))
                            .filter(tramList -> tramList.getList().size() > 0)
                            .flatMap(tramList -> Observable.fromIterable(tramList.getList())
                                    .filter(TramData::shouldBeVisible)
                                    .doOnNext(TramData::trimStrings)
                                    .subscribeOn(Schedulers.computation()))
                            .doOnNext(tramData -> mTramDataHashMap.put(tramData.getId(), tramData))
                            .toList()
                            .toObservable()
                            .retryWhen(errors -> Observable.zip(
                                    errors,
                                    Observable.just(1, 3, 5, 7),
                                    Pair::new)
                                    .flatMap(pair -> {
                                        if (pair.second > 5)
                                            return Observable.<Long>error(pair.first);
                                        return Observable.timer((long) pair.second, TimeUnit.SECONDS);
                                    }))
                            .onErrorResumeNext(throwable -> {
                                throwable.printStackTrace();
                                return Observable.just(new ArrayList<>());
                            });
                });
    }


    public void stopUpdates() {
        if (mDisposableRetrofit != null && !mDisposableRetrofit.isDisposed()) {
            mDisposableRetrofit.dispose();
        }
    }

    public void notifyJobDone() {
        ModelObserverInterface observer = mModelObserver.get();
        if (observer != null) {
            observer.notifyRefreshEnded();
            synchronized (mTramDataHashMap) {
                if (mTramDataHashMap.size() > 0) {
                    observer.updateMarkers(mTramDataHashMap);
                }
            }
        }
    }

    public List<FavoriteTramData> getFavoriteTramData() {
        SortedSet<FavoriteTramData> favoriteTrams = new TreeSet<>();
        for (String str : mFavoriteManager.getFavoriteTramData()) {
            favoriteTrams.add(new FavoriteTramData(str, true));
        }

        synchronized (mTramDataHashMap) {
            for (TramData tramData : mTramDataHashMap.values()) {
                if (!mFavoriteManager.isFavorite(tramData.getFirstLine())) {
                    favoriteTrams.add(new FavoriteTramData(tramData.getFirstLine(), false));
                }
            }
        }
        return new ArrayList<>(favoriteTrams);
    }

    private static class Holder {
        static final Model instance = new Model();
    }
}
