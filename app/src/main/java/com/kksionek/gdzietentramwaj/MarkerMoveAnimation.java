package com.kksionek.gdzietentramwaj;

import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.Queue;

public class MarkerMoveAnimation implements Runnable {

    private static final float TOTAL_ANIMATION_DURATION = 3000.0f;

    private static final LinearInterpolator mLatLngInterpolator = new LinearInterpolator();
    private static final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private static Handler mAnimHandler = null;
    private final long mStartTime;
    private final Marker mMarker;
    private final Polyline mPolyline;
    private final LatLng mAnimStartPos;
    private final LatLng mAnimEndPos;

    long mTimeElapsed;
    float mT;
    float mV;

    public MarkerMoveAnimation(TramMarker tramMarker, LatLng animEndPos, Handler handler) {
        mMarker = tramMarker.getMarker();
        mPolyline = tramMarker.getPolyline();
        mStartTime = SystemClock.uptimeMillis();
        mAnimStartPos = tramMarker.getMarkerPosition();
        mAnimEndPos = animEndPos;
        mAnimHandler = handler;
    }

    @Override
    public void run() {
        mTimeElapsed = SystemClock.uptimeMillis() - mStartTime;
        mT = mTimeElapsed / TOTAL_ANIMATION_DURATION;
        mV = mInterpolator.getInterpolation(mT);

        LatLng intermediatePosition = mLatLngInterpolator.interpolate(mV, mAnimStartPos, mAnimEndPos);
        mMarker.setPosition(intermediatePosition);
        Queue<LatLng> pointsQueue = new CircularFifoQueue<>(100);
        pointsQueue.addAll(mPolyline.getPoints());
        pointsQueue.add(intermediatePosition);
        mPolyline.setPoints(new ArrayList<>(pointsQueue));

        if (mT < 1)
            mAnimHandler.postDelayed(this, 16);
    }
}
