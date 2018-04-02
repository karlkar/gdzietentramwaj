package com.kksionek.gdzietentramwaj.view;

import android.content.Context;
import android.location.Location;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.kksionek.gdzietentramwaj.R;

public class AdProvider implements AdProviderInterface {

    private AdView mAdView;

    @Override
    public void showAd(ViewGroup adView) {
        mAdView = (AdView) adView;
    }

    @Override
    public void resume() {
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void pause() {
        if (mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void loadAd(Context context, Location location) {
        if (mAdView != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(context.getString(R.string.adMobTestDeviceS5))
                    .addTestDevice(context.getString(R.string.adMobTestDeviceS7))
                    .addTestDevice(context.getString(R.string.adMobTestDeviceS9plus))
                    .setLocation(location)
                    .build();
            mAdView.loadAd(adRequest);
        }
    }
}