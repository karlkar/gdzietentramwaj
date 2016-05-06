package com.kksionek.gdzietentramwaj;

import android.content.Context;
import android.support.annotation.UiThread;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;

public class TramMarker {

    private static final int POLYLINE_WIDTH = 5;

    private static IconGenerator mIconGenerator = null;
    private final TramData mTramData;
    private Marker mMarker;
    private Polyline mPolyline;
    private boolean mVisible;

    @UiThread
    public TramMarker(Context ctx, TramData tramData, GoogleMap map) {
        if (mIconGenerator == null)
            mIconGenerator = new IconGenerator(ctx);

        mTramData = tramData;

        mMarker = map.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(
                mIconGenerator.makeIcon(tramData.getFirstLine()))).position(tramData.getLatLng()));
        mPolyline = map.addPolyline(new PolylineOptions().add(tramData.getLatLng()).width(POLYLINE_WIDTH));
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

    public LatLng getMarkerPosition() {
        return mMarker.getPosition();
    }

    @UiThread
    public void setVisible(boolean visible) {
        mVisible = visible;
        mMarker.setVisible(visible);
        mPolyline.setVisible(visible);
    }

    @UiThread
    public void clearPolyline() {
        mPolyline.setPoints(new ArrayList<LatLng>());
    }

    @UiThread
    public void updateMarker(LatLng prevPosition, LatLng newPosition) {
        ArrayList<LatLng> points = new ArrayList<>();
        points.add(prevPosition);
        points.add(newPosition);
        mPolyline.setPoints(points);
        mMarker.setPosition(newPosition);
    }
}
