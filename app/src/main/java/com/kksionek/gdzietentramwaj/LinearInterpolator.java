package com.kksionek.gdzietentramwaj;


import com.google.android.gms.maps.model.LatLng;

public class LinearInterpolator {
    public LatLng interpolate(float fraction, LatLng a, LatLng b) {
        double lat = (b.latitude - a.latitude) * fraction + a.latitude;
        double lng = (b.longitude - a.longitude) * fraction + a.longitude;
        return new LatLng(lat, lng);
    }
}