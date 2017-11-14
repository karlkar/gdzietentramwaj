package com.kksionek.gdzietentramwaj.DataSource;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TramData {
    private static final String TAG = "TRAMDATA";

    @SerializedName("Time")
    private String mTime;

    @SerializedName("Lat")
    private double mLat;

    @SerializedName("Lon")
    private double mLng;

    @SerializedName("Lines")
    private String mFirstLine;

    @SerializedName("Brigade")
    private String mBrigade;

    private transient LatLng mLatLng = null;

    private boolean mTooOld = false;

    public String getId() { return mFirstLine + "/" + mBrigade; }

    public String getFirstLine() {
        return mFirstLine;
    }

    public String getTime() {
        return mTime;
    }

    public LatLng getLatLng() {
        if (mLatLng == null) {
            mLatLng = new LatLng(mLat, mLng);
        }
        return mLatLng;
    }

    public void setTooOld() {
        mTooOld = true;
    }

    public boolean isTooOld() {
        return mTooOld;
    }
}
