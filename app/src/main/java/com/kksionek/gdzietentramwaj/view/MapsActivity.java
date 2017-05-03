package com.kksionek.gdzietentramwaj.view;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.TramApplication;
import com.kksionek.gdzietentramwaj.data.TramData;
import com.kksionek.gdzietentramwaj.model.Geolocalizer;
import com.kksionek.gdzietentramwaj.model.Model;
import com.kksionek.gdzietentramwaj.model.PrefManager;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ModelObserverInterface, Geolocalizer.LocationUpdateListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";

    private final Model mModel = Model.getInstance();

    private GoogleMap mMap = null;
    private boolean mFirstLoad = true;
    private final HashMap<String, TramMarker> mTramMarkerHashMap = new HashMap<>();
    private boolean mFavoriteView;

    private MenuItemRefreshCtrl mMenuItemRefresh = null;
    private MenuItem mMenuItemFavoriteSwitch = null;
    private AdView mAdView;

    private IconGenerator mIconGenerator;
    private final ArrayList<TramMarker> mAnimationMarkers = new ArrayList<>();
    private final ValueAnimator mValueAnimator = ValueAnimator
            .ofFloat(0, 1)
            .setDuration(3000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mModel.setObserver(this, getApplicationContext(), ((TramApplication) getApplication()).getTramInterface());
        PrefManager.init(getApplicationContext());
        mFavoriteView = PrefManager.isFavoriteViewOn();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        checkLocationPermission();

        Location loc = new Location("");
        loc.setLatitude(52.231841);
        loc.setLongitude(21.005940);

        mAdView = (AdView) findViewById(R.id.adView);
        reloadAds(loc);

        mIconGenerator = new IconGenerator(this);
        mValueAnimator.addUpdateListener(animation -> {
            LatLng a, b, intermediatePos;
            Queue<LatLng> pointsQueue = new CircularFifoQueue<>(100);
            float fraction = animation.getAnimatedFraction();
            for (TramMarker tramMarker : mAnimationMarkers) {
                if (tramMarker.getMarker() == null)
                    continue;
                a = tramMarker.getPrevPosition();
                b = tramMarker.getFinalPosition();
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lng = (b.longitude - a.longitude) * fraction + a.longitude;
                intermediatePos = new LatLng(lat, lng);
                tramMarker.getMarker().setPosition(intermediatePos);

                pointsQueue.clear();
                List<LatLng> points = tramMarker.getPolyline().getPoints();
                if (!points.get(points.size() - 1).equals(intermediatePos)) {
                    pointsQueue.addAll(points);
                    pointsQueue.add(intermediatePos);
                    tramMarker.getPolyline().setPoints(new ArrayList<>(pointsQueue));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) {
            startFetchingData();
            updateMarkersVisibility();
        }
        if (mAdView != null)
            mAdView.resume();
        Geolocalizer geolocalizer = ((TramApplication) getApplication()).getGeolocalizer();
        geolocalizer.onResume();
        geolocalizer.addLocationUpdateListener(this);
    }

    @Override
    protected void onPause() {
        mModel.stopUpdates();
        if (mAdView != null)
            mAdView.pause();
        Geolocalizer geolocalizer = ((TramApplication) getApplication()).getGeolocalizer();
        geolocalizer.onPause();
        geolocalizer.removeLocationUpdateListener(this);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (menuItem != null)
            mMenuItemRefresh = new MenuItemRefreshCtrl(this, menuItem);

        mMenuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch);
        updateFavoriteSwitchIcon();

        new Handler().postDelayed(this::startFetchingData, 1000);

        return super.onCreateOptionsMenu(menu);
    }

    @UiThread
    private void updateFavoriteSwitchIcon() {
        if (mMenuItemFavoriteSwitch != null)
            mMenuItemFavoriteSwitch.setIcon(mFavoriteView ? R.drawable.fav_on : R.drawable.fav_off);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("O aplikacji");
            builder.setMessage("Dane wykorzystywane w aplikacji są dostarczane przez Miasto Stołeczne Warszawa za pośrednictwem serwisu http://api.um.warszawa.pl");
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();
            return true;
        } else if (item.getItemId() == R.id.menu_item_refresh) {
            startFetchingData();
            return true;
        } else if (item.getItemId() == R.id.menu_item_favorite) {
            Intent intent = new Intent(this, FavoriteLinesActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_item_favorite_switch) {
            mFavoriteView = !mFavoriteView;
            updateFavoriteSwitchIcon();
            PrefManager.setFavoriteViewOn(mFavoriteView);
            updateMarkersVisibility();
        }
        return super.onOptionsItemSelected(item);
    }

    @UiThread
    private void startFetchingData() {
        mMenuItemRefresh.startAnimation();
        mModel.startFetchingData();
    }

    @UiThread
    private void updateMarkersVisibility() {
        for (TramMarker marker : mTramMarkerHashMap.values()) {
            updateMarkerVisibility(marker);
            if (marker.isVisible(mMap)) {
                createNewFullMarker(marker);
            } else {
                marker.remove();
            }
        }
    }

    private void updateMarkerVisibility(TramMarker tramMarker) {
        tramMarker.setVisible(!mFavoriteView || mModel.getFavoriteManager().isFavorite(tramMarker.getTramLine()));
    }

    @Override
    @UiThread
    public void notifyRefreshStarted() {
        if (mMenuItemRefresh == null)
            return;
        mMenuItemRefresh.startAnimation();
    }

    @Override
    @UiThread
    public void notifyRefreshEnded() {
        if (mMenuItemRefresh == null)
            return;
        mMenuItemRefresh.endAnimation();
    }

    @Override
    @UiThread
    public void updateMarkers(@NonNull HashMap<String, TramData> tramDataHashMap) {
        Toast.makeText(getApplicationContext(), "Aktualizacja pozycji pojazdów", Toast.LENGTH_SHORT).show();

        updateExistingMarkers(tramDataHashMap);

        if (tramDataHashMap.size() == mTramMarkerHashMap.size())
            return;

        addNewMarkers(tramDataHashMap);
    }

    @UiThread
    private void updateExistingMarkers(@NonNull HashMap<String, TramData> tramDataHashMap) {
        Iterator<Map.Entry<String, TramMarker>> iter = mTramMarkerHashMap.entrySet().iterator();
        mAnimationMarkers.clear();
        while (iter.hasNext()) {
            Map.Entry<String, TramMarker> element = iter.next();
            TramMarker tramMarker = element.getValue();
            if (tramDataHashMap.containsKey(element.getKey())) {
                TramData updatedData = tramDataHashMap.get(element.getKey());

                LatLng prevPosition = tramMarker.getFinalPosition();
                LatLng newPosition = updatedData.getLatLng();
                if (prevPosition.equals(newPosition))
                    continue;

                tramMarker.updatePosition(newPosition);
                updateMarkerVisibility(tramMarker);

                if (tramMarker.isVisible(mMap)) {
                    if (tramMarker.getMarker() == null) {
                        Marker m = mMap.addMarker(new MarkerOptions()
                                .position(tramMarker.getPrevPosition())
                                .icon(TramMarker.getBitmap(tramMarker.getTramLine(), mIconGenerator)));
                        tramMarker.setMarker(m);
                    }
                    if (tramMarker.getPolyline() == null) {
                        Polyline p = mMap.addPolyline(new PolylineOptions()
                                .add(tramMarker.getPrevPosition())
                                .color(Color.argb(255, 236, 57, 57))
                                .width(TramMarker.POLYLINE_WIDTH));
                        tramMarker.setPolyline(p);
                    }
                    mAnimationMarkers.add(tramMarker);
                } else
                    tramMarker.remove();
            } else {
                tramMarker.remove();
                iter.remove();
            }
        }
        if (!mAnimationMarkers.isEmpty())
            mValueAnimator.start();
    }

    @UiThread
    private void addNewMarkers(@NonNull HashMap<String, TramData> tramDataHashMap) {
        for (Map.Entry<String, TramData> element : tramDataHashMap.entrySet()) {
            if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                TramMarker tramMarker = new TramMarker(element.getValue());
                updateMarkerVisibility(tramMarker);
                mTramMarkerHashMap.put(element.getKey(), tramMarker);
                if (tramMarker.isVisible(mMap)) {
                    createNewFullMarker(tramMarker);
                } else {
                    tramMarker.remove();
                }
            }
        }
    }

    private void createNewFullMarker(TramMarker tramMarker) {
        if (tramMarker.getMarker() == null) {
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(tramMarker.getFinalPosition())
                    .icon(TramMarker.getBitmap(tramMarker.getTramLine(), mIconGenerator)));
            tramMarker.setMarker(m);
        }
        if (tramMarker.getPolyline() == null) {
            Polyline p = mMap.addPolyline(new PolylineOptions()
                    .add(tramMarker.getPrevPosition())
                    .add(tramMarker.getFinalPosition())
                    .color(Color.argb(255, 236, 57, 57))
                    .width(TramMarker.POLYLINE_WIDTH));
            tramMarker.setPolyline(p);
        }
    }

    @UiThread
    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMinZoomPreference(14.5f);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mMap.setMyLocationEnabled(
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED);
        else
            mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        LatLng warsaw = new LatLng(52.231841, 21.005940);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 15));
        Geolocalizer geolocalizer = ((TramApplication)getApplication()).getGeolocalizer();
        Location location = geolocalizer.getLastLocation();
        if (location != null) {
            onLocationUpdated(location);
        }
        mMap.setOnMarkerClickListener(marker -> true);
        mMap.setOnCameraIdleListener(() -> {
            for (TramMarker marker : mTramMarkerHashMap.values()) {
                if (marker.isVisible(mMap)) {
                    createNewFullMarker(marker);
                } else {
                    marker.remove();
                }
            }
        });
    }

    private void reloadAds(Location location) {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .addTestDevice(getString(R.string.adMobTestDeviceS7))
                .setLocation(location)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ((TramApplication)getApplication()).getGeolocalizer().onResume();
                mMap.setMyLocationEnabled(
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mFirstLoad) {
            reloadAds(location);
            mFirstLoad = false;
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
        }
    }
}
