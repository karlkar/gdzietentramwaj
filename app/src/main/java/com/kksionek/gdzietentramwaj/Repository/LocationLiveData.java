package com.kksionek.gdzietentramwaj.Repository;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

class LocationLiveData extends LiveData<Location> {
    private static LocationLiveData sInstance = null;
    private LocationManager mLocationManager;

    public static LocationLiveData getInstance(Context context) {
        if (sInstance == null)
            sInstance = new LocationLiveData(context);
        return sInstance;
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            setValue(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private LocationLiveData(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActive() {
        setValue(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                100,
                0,
                mLocationListener);
    }

    @Override
    protected void onInactive() {
        mLocationManager.removeUpdates(mLocationListener);
    }
}
