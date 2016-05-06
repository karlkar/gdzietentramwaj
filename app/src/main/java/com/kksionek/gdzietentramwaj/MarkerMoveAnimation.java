package com.kksionek.gdzietentramwaj;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.List;

public class MarkerMoveAnimation implements Runnable {

    private static final LinearInterpolator mLatLngInterpolator = new LinearInterpolator();
    private static final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private static Handler mAnimHandler = null;
    private long mStartTime;
    private final Marker mMarker;
    private final Polyline mPolyline;
    private final LatLng mAnimStartPos;
    private final LatLng mAnimEndPos;

    long mTimeElapsed;
    float mT;
    float mV;

    public MarkerMoveAnimation(TramMarker tramMarker, long startTime, LatLng animEndPos, Handler handler) {
        mMarker = tramMarker.getMarker();
        mPolyline = tramMarker.getPolyline();
        mStartTime = startTime;
        mAnimStartPos = tramMarker.getMarkerPosition();
        mAnimEndPos = animEndPos;
        mAnimHandler = handler;
    }

    @Override
    public void run() {
        mTimeElapsed = SystemClock.uptimeMillis() - mStartTime;
        mT = mTimeElapsed / 3000.0f;
        mV = mInterpolator.getInterpolation(mT);

        LatLng intermediatePosition = mLatLngInterpolator.interpolate(mV, mAnimStartPos, mAnimEndPos);
        mMarker.setPosition(intermediatePosition);
        List<LatLng> points = mPolyline.getPoints();
        points.add(intermediatePosition);
        mPolyline.setPoints(points);

        if (mT < 1)
            mAnimHandler.postDelayed(this, 16);
    }
}
