package com.kksionek.gdzietentramwaj;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";

    private final Model mModel = new Model(this);

    private GoogleMap mMap;
    private IconGenerator mIconGenerator;
    private final HashMap<String, Pair<Marker, Polyline>> mTramMarkerHashMap = new HashMap<>();
    private final HashMap<Marker, String> mMarkerTramIdMap = new HashMap<>();
    private final LinearInterpolator mLatLngInterpolator = new LinearInterpolator();
    private final Handler handler = new Handler();

    private MenuItemRefreshCtrl mMenuItemRefresh = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mIconGenerator = new IconGenerator(this);

        checkLocationPermission();

        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceNote5))
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .build();
        adView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mModel.startUpdates();
    }

    @Override
    protected void onPause() {
        mModel.stopUpdates();
        super.onPause();
    }

    public void notifyRefreshStarted() {
        if (mMenuItemRefresh == null)
            return;
        mMenuItemRefresh.startAnimation();
    }

    public void notifyRefreshEnded() {
        if (mMenuItemRefresh == null)
            return;
        mMenuItemRefresh.endAnimation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (menuItem != null)
            mMenuItemRefresh = new MenuItemRefreshCtrl(this, menuItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("O aplikacji");
            builder.setMessage("Dane wykorzystywane w aplikacji są dostarczane przez Miasto Stołeczne Warszawa za pośrednictwem serwisu http://api.um.warszawa.pl");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
            return true;
        } else if (item.getItemId() == R.id.menu_item_refresh) {
            mModel.startUpdates();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMarkers(HashMap<String, TramData> tramDataHashMap) {
        Toast.makeText(getApplicationContext(), "Aktualizacja pozycji tramwajów", Toast.LENGTH_SHORT).show();

        Iterator<Map.Entry<String, Pair<Marker, Polyline>>> iter = mTramMarkerHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, Pair<Marker, Polyline>> element = iter.next();
            final Marker marker = element.getValue().first;
            final Polyline polyline = element.getValue().second;
            if (tramDataHashMap.containsKey(element.getKey())) {
                TramData updatedData = tramDataHashMap.get(element.getKey());
                LatLng prevPosition = updatedData.getPrevLatLng();
                final LatLng newPosition = updatedData.getLatLng();
                if (prevPosition.equals(newPosition))
                    continue;
                final LatLng animStartPosition = marker.getPosition();
                polyline.setPoints(new ArrayList<LatLng>());
                if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(marker.getPosition())
                        || mMap.getProjection().getVisibleRegion().latLngBounds.contains(newPosition)) {
                    final long start = SystemClock.uptimeMillis();
                    final Interpolator interpolator = new AccelerateDecelerateInterpolator();

                    handler.post(new Runnable() {
                        long elapsed;
                        float t;
                        float v;

                        @Override
                        public void run() {
                            elapsed = SystemClock.uptimeMillis() - start;
                            t = elapsed / 3000.0f;
                            v = interpolator.getInterpolation(t);

                            LatLng intermediatePosition = mLatLngInterpolator.interpolate(v, animStartPosition, newPosition);
                            marker.setPosition(intermediatePosition);
                            List<LatLng> points = polyline.getPoints();
                            points.add(intermediatePosition);
                            polyline.setPoints(points);

                            if (t < 1)
                                handler.postDelayed(this, 16);
                        }
                    });
                } else {
                    List<LatLng> points = polyline.getPoints();
                    points.add(prevPosition);
                    points.add(newPosition);
                    polyline.setPoints(points);
                    marker.setPosition(newPosition);
                }
            } else {
                mMarkerTramIdMap.remove(marker);
                marker.remove();
                polyline.remove();
                iter.remove();
            }
        }

        if (tramDataHashMap.size() == mTramMarkerHashMap.size())
            return;

        for (Map.Entry<String, TramData> element : tramDataHashMap.entrySet()) {
            if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                LatLng newPosition = element.getValue().getLatLng();
                Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(
                        mIconGenerator.makeIcon(element.getValue().getFirstLine()))).position(newPosition));
                Polyline polyline = mMap.addPolyline(new PolylineOptions().add(newPosition).width(5));
                mTramMarkerHashMap.put(element.getKey(), new Pair<>(marker, polyline));
                mMarkerTramIdMap.put(marker, element.getKey());
            }
        }
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else
                mMap.setMyLocationEnabled(false);
        } else
            mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        LatLng warsaw = new LatLng(52.231841, 21.005940);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 15));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMap.setOnMyLocationChangeListener(null);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    mMap.setMyLocationEnabled(true);
                else
                    mMap.setMyLocationEnabled(false);
            }
        }
    }
}
