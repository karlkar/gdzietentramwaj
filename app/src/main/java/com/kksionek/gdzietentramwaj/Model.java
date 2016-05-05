package com.kksionek.gdzietentramwaj;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

public class Model {

    private static final String TAG = "MODEL";
    private static final String WARSZAWA_TRAM_API = "https://api.um.warszawa.pl/api/action/wsstore_get/?id=c7238cfe-8b1f-4c38-bb4a-de386db7e776&apikey=***REMOVED***";

    private final HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private FavoriteManager mFavoriteManager;
    private MapsActivity mMapsActivityObserver;

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
        synchronized (mTramDataHashMap) {
            for (TramData tramData : mTramDataHashMap.values())
                favoriteTrams.add(new FavoriteTramData(tramData.getFirstLine(), mFavoriteManager.isFavorite(tramData.getFirstLine())));
        }
        return new ArrayList<>(favoriteTrams);
    }

    private static class Holder {
        static final Model instance = new Model();
    }

    public class FavoriteManager {

        private static final String PREF_FAVORITE_TRAMS = "PREF_FAVORITE_TRAMS";
        private Context mCtx;
        private SharedPreferences mSharedPreferences;
        private Set<String> mFavoriteTramData;

        FavoriteManager(Context ctx) {
            mCtx = ctx;
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mCtx);
            mFavoriteTramData = mSharedPreferences.getStringSet(PREF_FAVORITE_TRAMS, new HashSet<String>());
        }

        public boolean isFavorite(String line) {
            return mFavoriteTramData.contains(line);
        }

        public void setFavorite(String line, boolean favorite) {
            if (favorite)
                mFavoriteTramData.add(line);
            else
                mFavoriteTramData.remove(line);
            mSharedPreferences.edit().remove(PREF_FAVORITE_TRAMS).putStringSet(PREF_FAVORITE_TRAMS, mFavoriteTramData).apply();
        }

    }
}
