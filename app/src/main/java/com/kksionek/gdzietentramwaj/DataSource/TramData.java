package com.kksionek.gdzietentramwaj.DataSource;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

public class TramData {
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

    public String getId() { return mFirstLine + "/" + mBrigade; }

    public String getFirstLine() {
        return mFirstLine;
    }

    public LatLng getLatLng() {
        if (mLatLng == null)
            mLatLng = new LatLng(Double.valueOf(mLat), Double.valueOf(mLng));
        return mLatLng;
    }

    public void trimStrings() {
        mFirstLine = mFirstLine.trim();
        mBrigade = mBrigade.trim();
    }
}
