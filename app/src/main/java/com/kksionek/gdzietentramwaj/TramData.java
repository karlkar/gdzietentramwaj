package com.kksionek.gdzietentramwaj;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TramData {

    private static final String TAG = "TRAMDATA";
    private transient static final int MIN_LATITUDE = 45;
    private transient static final int MIN_LONGITUDE = 10;

    @SerializedName("Time")
    private String mTime;

    @SerializedName("Lat")
    private String mLat;

    @SerializedName("Lon")
    private String mLng;

    @SerializedName("FirstLine")
    private String mFirstLine;

    @SerializedName("Brigade")
    private String mBrigade;

    @SerializedName("Status")
    private String mStatus;

    @SerializedName("LowFloor")
    private boolean mLowFloor;

    private transient LatLng mLatLng = null;
    private transient LatLng mPrevLatLng = null;

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

    public String getStatus() {
        return mStatus;
    }

    public boolean isLowFloor() {
        return mLowFloor;
    }

    public LatLng getLatLng() {
        if (mLatLng == null)
            mLatLng = new LatLng(Double.valueOf(mLat), Double.valueOf(mLng));
        return mLatLng;
    }

    public LatLng getPrevLatLng() {
        return mPrevLatLng;
    }

    public boolean isRunning() { return mStatus.equals("RUNNING"); }

    public void updatePosition(TramData tramData) {
        mPrevLatLng = mLatLng;
        mLatLng = tramData.mLatLng;
    }

    public boolean shouldBeVisible() {
        // Sometimes trams have position outside of Poland (in most cases it is 0, 0)
        return isRunning() && getLatLng().latitude > MIN_LATITUDE && getLatLng().longitude > MIN_LONGITUDE;
    }
}
