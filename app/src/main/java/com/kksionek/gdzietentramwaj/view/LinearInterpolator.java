package com.kksionek.gdzietentramwaj.view;


import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

public class LinearInterpolator {
    public LatLng interpolate(float fraction, @NonNull LatLng a, @NonNull LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }
}