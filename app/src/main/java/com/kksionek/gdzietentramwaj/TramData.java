package com.kksionek.gdzietentramwaj;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

public class TramData {

    private static final String TAG = "TRAMDATA";
    private static final int MIN_LATITUDE = 45;
    private static final int MIN_LONGITUDE = 10;

    public String getId() { return mId; }

    public String getStatus() {
        return mStatus;
    }

    public String getFirstLine() {
        return mFirstLine;
    }

    public LatLng getLatLng() {
        return mLatLng;
    }

    public LatLng getPrevLatLng() {
        return mPrevLatLng;
    }

    public String getTime() {
        return mTime;
    }

    public boolean isLowFloor() {
        return mLowFloor;
    }

    public boolean isRunning() { return mStatus.equals("RUNNING"); }

    private String mId;
    private String mStatus;
    private String mFirstLine;
    private LatLng mLatLng;
    private LatLng mPrevLatLng;
    private String mTime;
    private boolean mLowFloor;

    public TramData(JSONObject jsonObject) throws JSONException {
        mStatus = jsonObject.getString("Status");
        mFirstLine = jsonObject.getString("FirstLine").trim();
        mLatLng = new LatLng(jsonObject.getDouble("Lat"), jsonObject.getDouble("Lon"));
        mPrevLatLng = mLatLng;
        mTime = jsonObject.getString("Time");
        mLowFloor = jsonObject.getBoolean("LowFloor");
        String brigade = jsonObject.getString("Brigade");
        mId = mFirstLine + "/" + brigade;
    }

    public void updatePosition(TramData tramData) {
        mPrevLatLng = mLatLng;
        mLatLng = tramData.mLatLng;
    }

    public boolean shouldBeVisible() {
        // Sometimes trams have position outside of Poland (in most cases it is 0, 0)
        return isRunning() && mLatLng.latitude > MIN_LATITUDE && mLatLng.longitude > MIN_LONGITUDE;
    }
}
