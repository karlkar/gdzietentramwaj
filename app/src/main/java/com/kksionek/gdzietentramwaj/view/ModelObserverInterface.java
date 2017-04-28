package com.kksionek.gdzietentramwaj.view;

import android.support.annotation.NonNull;

import com.kksionek.gdzietentramwaj.data.TramData;

import java.util.HashMap;

public interface ModelObserverInterface {

    void notifyRefreshStarted();
    void notifyRefreshEnded();
    void updateMarkers(@NonNull HashMap<String, TramData> tramDataHashMap);
}
