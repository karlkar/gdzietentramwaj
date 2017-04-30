package com.kksionek.gdzietentramwaj.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class Geolocalizer implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public interface LocationUpdateListener {
        void onLocationUpdated(Location location);
    }

    private GoogleApiClient mGoogleApi = null;
    private Location mLastLocation = null;
    private LocationRequest mLocationRequest;
    private boolean mConnected = false;
    private boolean mLocationUpdates = false;
    private final ArrayList<WeakReference<LocationUpdateListener>> mLocationUpdateListeners = new ArrayList<>();
    private Context mCtx;

    public Geolocalizer(Context ctx) {
        mCtx = ctx;
        if (mGoogleApi == null) {
            mGoogleApi = new GoogleApiClient.Builder(mCtx)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(20000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void onStart() {
        mGoogleApi.connect();
    }

    public void onStop() {
        mGoogleApi.disconnect();
    }

    public void onResume() {
        startLocationUpdates();
    }

    public void onPause() {
        stopLocationUpdates();
    }

    public void addLocationUpdateListener(LocationUpdateListener listener) {
        synchronized (mLocationUpdateListeners) {
            mLocationUpdateListeners.add(new WeakReference<>(listener));
        }
    }

    public void removeLocationUpdateListener(LocationUpdateListener listener) {
        synchronized (mLocationUpdateListeners) {
            mLocationUpdateListeners.removeIf(ref -> ref.get() == null || ref.get() == listener);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mConnected = true;
        if (ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApi);
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mCtx, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (mConnected && !mLocationUpdates) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApi, mLocationRequest, this);
            mLocationUpdates = true;
        }
    }

    private void stopLocationUpdates() {
        if (mConnected && mLocationUpdates) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApi, this);
            mLocationUpdates = false;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mConnected = false;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mConnected = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LocationUpdateListener listener;
        synchronized (mLocationUpdateListeners) {
            for (WeakReference<LocationUpdateListener> ref : mLocationUpdateListeners) {
                listener = ref.get();
                if (listener != null)
                    listener.onLocationUpdated(location);
                else
                    mLocationUpdateListeners.remove(ref);
            }
        }
    }

    public Location getLastLocation() {
        return mLastLocation;
    }
}
