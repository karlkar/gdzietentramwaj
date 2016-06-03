package com.kksionek.gdzietentramwaj;

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

public class Model {

    private static final String TAG = "MODEL";
    private static final String WARSZAWA_TRAM_API = "https://api.um.warszawa.pl/api/action/wsstore_get/?id=c7238cfe-8b1f-4c38-bb4a-de386db7e776&apikey=***REMOVED***";

    private final HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private FavoriteManager mFavoriteManager = null;
    private MapsActivity mMapsActivityObserver = null;

    private final Object mDataLoaderMutex = new Object();
    private TramLoader mDataLoader = null;
    private Timer mTimer;

    private Model() {}

    public static Model getInstance() {
        return Holder.instance;
    }

    public FavoriteManager getFavoriteManager() {
        return mFavoriteManager;
    }

    public void setObserver(MapsActivity observer) {
        mMapsActivityObserver = observer;
        if (mFavoriteManager == null)
            mFavoriteManager = new FavoriteManager(observer);
    }

    private void createAndStartLoaderTask() {
        synchronized (mDataLoaderMutex) {
            if (mDataLoader == null || mDataLoader.isDone()) {
                mDataLoader = new TramLoader(WARSZAWA_TRAM_API, this);
                mMapsActivityObserver.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDataLoader.launch();
                    }
                });
            }
        }
    }

    public void startUpdates() {
        Log.d(TAG, "startUpdates: START");
        if (mTimer != null) {
            mTimer.cancel();
            mTimer.purge();
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                createAndStartLoaderTask();
            }
        }, 10, 30000);
    }

    public void stopUpdates() {
        if (mTimer != null)
            mTimer.cancel();
    }

    public void notifyRefreshStarted() {
        mMapsActivityObserver.notifyRefreshStarted();
    }

    public void notifyJobDone(boolean result) {
        if (result) {
            mMapsActivityObserver.notifyRefreshEnded();
            synchronized (mTramDataHashMap) {
                mMapsActivityObserver.updateMarkers(mTramDataHashMap);
            }
        } else
            startUpdates();
    }

    public void update(HashMap<String, TramData> map) {
        Iterator<Map.Entry<String, TramData>> iter = mTramDataHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TramData> tmp = iter.next();
            if (!map.containsKey(tmp.getValue().getId()))
                iter.remove();
        }

        for (TramData tramData : map.values()) {
            if (mTramDataHashMap.containsKey(tramData.getId()))
                mTramDataHashMap.get(tramData.getId()).updatePosition(tramData);
            else
                mTramDataHashMap.put(tramData.getId(), tramData);
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
