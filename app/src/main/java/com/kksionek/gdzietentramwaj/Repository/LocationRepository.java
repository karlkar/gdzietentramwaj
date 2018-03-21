package com.kksionek.gdzietentramwaj.Repository;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.tasks.Task;

import javax.inject.Inject;

public class LocationRepository {
    private LocationGetter mLocationGetter;

    @Inject
    public LocationRepository(Context context) {
        mLocationGetter = new LocationGetter(context);
    }

    public Task<Location> getLastKnownLocation() {
        return mLocationGetter.getLastKnownLocation();
    }
}
