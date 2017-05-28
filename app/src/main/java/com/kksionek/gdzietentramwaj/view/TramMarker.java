package com.kksionek.gdzietentramwaj.view;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.ui.IconGenerator;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import java.util.HashMap;

public class TramMarker {

    public static final int POLYLINE_WIDTH = 8;

    private static final HashMap<String, BitmapDescriptor> mBitmaps = new HashMap<>();

    private final TramData mTramData;
    private Marker mMarker = null;
    private Polyline mPolyline = null;

    private LatLng mPrevPosition;
    private LatLng mFinalPosition;
    private boolean mVisible;

    @UiThread
    public TramMarker(@NonNull TramData tramData) {
        mTramData = tramData;
        mPrevPosition = tramData.getLatLng();
        mFinalPosition = tramData.getLatLng();
    }

    public void setMarker(Marker marker) {
        if (mMarker != null)
            mMarker.remove();
        mMarker = marker;
    }

    public void setPolyline(Polyline polyline) {
        if (mPolyline != null)
            mPolyline.remove();
        mPolyline = polyline;
    }

    @UiThread
    public void remove() {
        if (mMarker != null) {
            mMarker.remove();
            mMarker = null;
        }
        if (mPolyline != null) {
            mPolyline.remove();
            mPolyline = null;
        }
    }

    public String getTramLine() {
        return mTramData.getFirstLine();
    }

    public boolean isVisible(@Nullable GoogleMap map) {
        LatLngBounds bounds = null;
        if (map != null) {
            bounds = map.getProjection().getVisibleRegion().latLngBounds;
        }
        return mVisible
                && bounds != null
                && (bounds.contains(mFinalPosition) || bounds.contains(mPrevPosition));
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

    public void updatePosition(LatLng finalPosition) {
        mPrevPosition = mFinalPosition;
        mFinalPosition = finalPosition;
    }

    @UiThread
    public void setVisible(boolean visible) {
        mVisible = visible;
        if (mMarker != null)
            mMarker.setVisible(visible);
        if (mPolyline != null)
            mPolyline.setVisible(visible);
    }

    public static BitmapDescriptor getBitmap(String line, @NonNull IconGenerator iconGenerator) {
        BitmapDescriptor bitmapDescriptor = mBitmaps.get(line);
        if (bitmapDescriptor == null) {
            if (line.length() < 3)
                iconGenerator.setColor(Color.argb(255, 249, 245, 206));
            else
                iconGenerator.setColor(Color.WHITE);
            bitmapDescriptor =
                    BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(line));
            mBitmaps.put(line, bitmapDescriptor);
        }
        return bitmapDescriptor;
    }

    public LatLng getPrevPosition() {
        return mPrevPosition;
    }
}
