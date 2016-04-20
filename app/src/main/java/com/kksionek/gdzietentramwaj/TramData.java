package com.kksionek.gdzietentramwaj;

import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.json.JSONObject;

public class TramData {

    public String getId() { return mId; }

    public String getStatus() {
        return mStatus;
    }

    public String getFirstLine() {
        return mFirstLine;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public String getTime() {
        return mTime;
    }

    public boolean isLowFloor() {
        return mLowFloor;
    }

    private String mId;
    private String mStatus;
    private String mFirstLine;
    private double mLat;
    private double mLon;
    private String mTime;
    private boolean mLowFloor;
    private String mBrigade;

    public TramData(JSONObject jsonObject) throws JSONException {
        mStatus = jsonObject.getString("Status");
        mFirstLine = jsonObject.getString("FirstLine").trim();
        mLat = jsonObject.getDouble("Lat");
        mLon = jsonObject.getDouble("Lon");
        mTime = jsonObject.getString("Time");
        mLowFloor = jsonObject.getBoolean("LowFloor");
        mBrigade = jsonObject.getString("Brigade");
        mId = mFirstLine + "/" + mBrigade;
    }

    public void updatePosition(TramData tramData) {
        mLat = tramData.mLat;
        mLon = tramData.mLon;
    }
}
