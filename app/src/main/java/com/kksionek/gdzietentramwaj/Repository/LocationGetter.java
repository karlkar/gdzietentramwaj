package com.kksionek.gdzietentramwaj.Repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

class LocationGetter {
    private final FusedLocationProviderClient mProviderClient;

    LocationGetter(Context context) {
        mProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    @SuppressLint("MissingPermission")
    Task<Location> getLastKnownLocation() {
        return mProviderClient.getLastLocation();
    }
}
