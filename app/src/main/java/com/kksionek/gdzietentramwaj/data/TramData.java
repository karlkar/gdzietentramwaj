package com.kksionek.gdzietentramwaj.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TramData {
    public transient static final int DISTANCE_CLOSE = 4000;

    private static final String TAG = "TRAMDATA";

//    @SerializedName("Time")
//    private String mTime;

    @SerializedName("Lat")
    private String mLat;

    @SerializedName("Lon")
    private String mLng;

    @SerializedName("Lines")
    private String mFirstLine;

    @SerializedName("Brigade")
    private String mBrigade;

    private transient LatLng mLatLng = null;

    private transient Location mLocation = null;

    public String getId() { return mFirstLine + "/" + mBrigade; }

    public String getFirstLine() {
        return mFirstLine;
    }

    public LatLng getLatLng() {
        if (mLatLng == null)
            mLatLng = new LatLng(Double.valueOf(mLat), Double.valueOf(mLng));
        return mLatLng;
    }

    public Location getLocation() {
        if (mLocation == null) {
            mLocation = new Location("NONE");
            LatLng latLng = getLatLng();
            mLocation.setLatitude(latLng.latitude);
            mLocation.setLongitude(latLng.longitude);
        }
        return mLocation;
    }

    public void trimStrings() {
        mFirstLine = mFirstLine.trim();
        mBrigade = mBrigade.trim();
    }

    public boolean isCloseTo(Location lastLocation) {
        if (lastLocation == null)
            return true;
        return getLocation().distanceTo(lastLocation) < DISTANCE_CLOSE;
    }

}
