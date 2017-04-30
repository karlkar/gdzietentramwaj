package com.kksionek.gdzietentramwaj.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.data.TramData;

import java.util.ArrayList;
import java.util.HashMap;

public class TramMarker {

    private static final int POLYLINE_WIDTH = 8;

    private static IconGenerator mIconGenerator = null;
    private static HashMap<String, BitmapDescriptor> mBitmaps = new HashMap<>();

    private final TramData mTramData;
    private final Marker mMarker;
    private final Polyline mPolyline;

    private LatLng mFinalPosition;
    private boolean mVisible;

    @UiThread
    public TramMarker(@NonNull Context ctx, @NonNull TramData tramData, @NonNull GoogleMap map) {
        if (mIconGenerator == null)
            mIconGenerator = new IconGenerator(ctx);

        mTramData = tramData;

        BitmapDescriptor bitmapDescriptor = mBitmaps.get(tramData.getFirstLine());
        if (bitmapDescriptor == null) {
            if (tramData.getFirstLine().length() < 3)
                mIconGenerator.setColor(ContextCompat.getColor(ctx, R.color.tramColor));
            else
                mIconGenerator.setColor(Color.WHITE);
            bitmapDescriptor =
                    BitmapDescriptorFactory.fromBitmap(mIconGenerator.makeIcon(tramData.getFirstLine()));
            mBitmaps.put(tramData.getFirstLine(), bitmapDescriptor);
        }

        mMarker = map.addMarker(new MarkerOptions()
                .icon(bitmapDescriptor)
                .position(tramData.getLatLng()));
        mPolyline = map.addPolyline(
                new PolylineOptions()
                        .add(tramData.getLatLng())
                        .color(ContextCompat.getColor(ctx, R.color.polylineColor))
                        .width(POLYLINE_WIDTH));
    }

    @UiThread
    public void remove() {
        mMarker.remove();
        mPolyline.remove();
    }

    public String getTramLine() {
        return mTramData.getFirstLine();
    }

    public boolean isVisible() {
        return mVisible;
    }

    public Marker getMarker() {
        return mMarker;
    }

    public Polyline getPolyline() {
        return mPolyline;
    }

    public LatLng getFinalPosition() {
        return mFinalPosition;
    }

    public void setFinalPosition(LatLng finalPosition) {
        mFinalPosition = finalPosition;
    }

    @UiThread
    public void setVisible(boolean visible) {
        mVisible = visible;
        mMarker.setVisible(visible);
        mPolyline.setVisible(visible);
    }

    @UiThread
    public void updateMarker(@NonNull LatLng prevPosition, @NonNull LatLng newPosition) {
        ArrayList<LatLng> points = new ArrayList<>();
        points.add(prevPosition);
        points.add(newPosition);
        mPolyline.setPoints(points);
        mMarker.setPosition(newPosition);
    }
}
