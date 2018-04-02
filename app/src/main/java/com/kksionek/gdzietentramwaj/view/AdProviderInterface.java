package com.kksionek.gdzietentramwaj.view;

import android.content.Context;
import android.location.Location;
import android.view.ViewGroup;

public interface AdProviderInterface {
    void showAd(ViewGroup adView);
    void resume();
    void pause();
    void loadAd(Context context, Location location);
}
