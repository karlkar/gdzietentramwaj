package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.location.Location;

import javax.inject.Inject;

public class LocationRepository {
    private LocationLiveData mLocationLiveData;

    @Inject
    public LocationRepository(Context context) {
        mLocationLiveData = LocationLiveData.getInstance(context);
    }

    public LiveData<Location> getLocationLiveData() {
        return mLocationLiveData;
    }
}
