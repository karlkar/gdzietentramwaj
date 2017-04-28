package com.kksionek.gdzietentramwaj.data;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TramData {

    private static final String TAG = "TRAMDATA";
    private transient static final int MIN_LATITUDE = 45;
    private transient static final int MIN_LONGITUDE = 10;
    private transient static final int DISTANCE_CLOSE = 5000;

    @SerializedName("Time")
    private String mTime;

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

    public String getTime() {
        return mTime;
    }

    public String getLat() { return mLat; }

    public String getLng() { return mLng; }

    public String getFirstLine() {
        return mFirstLine;
    }

    public String getBrigade() { return mBrigade; }

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

    public boolean shouldBeVisible() {
        // Sometimes trams have position outside of Poland (in most cases it is 0, 0)
        return getLatLng().latitude > MIN_LATITUDE && getLatLng().longitude > MIN_LONGITUDE;
    }

    public boolean isCloseTo(Location lastLocation) {
        if (lastLocation == null)
            return true;
        return getLocation().distanceTo(lastLocation) < DISTANCE_CLOSE;
    }

}
