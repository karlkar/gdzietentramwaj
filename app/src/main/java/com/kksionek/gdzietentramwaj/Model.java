package com.kksionek.gdzietentramwaj;

import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Model {

    private static final String TAG = "MODEL";
    private static final String WARSZAWA_TRAM_API = "https://api.um.warszawa.pl/api/action/wsstore_get/?id=c7238cfe-8b1f-4c38-bb4a-de386db7e776&apikey=***REMOVED***";

    private final HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private MapsActivity mObserver;

    private final Object mDataLoaderMutex = new Object();
    private TramLoader mDataLoader = null;
    private Timer mTimer;

    public Model(MapsActivity observer) {
        mObserver = observer;
    }

    private void createAndStartLoaderTask() {
        synchronized (mDataLoaderMutex) {
            if (mDataLoader == null || mDataLoader.isDone()) {
                mDataLoader = new TramLoader(WARSZAWA_TRAM_API, this);
                mObserver.runOnUiThread(new Runnable() {
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
        mObserver.notifyRefreshStarted();
    }

    public void notifyJobDone(boolean result) {
        if (result) {
            mObserver.notifyRefreshEnded();
            synchronized (mTramDataHashMap) {
                mObserver.updateMarkers(mTramDataHashMap);
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
}
