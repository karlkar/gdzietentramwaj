package com.kksionek.gdzietentramwaj.view;

import android.Manifest;
import android.animation.ValueAnimator;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.ViewModel.MainActivityViewModel;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import org.apache.commons.collections4.queue.CircularFifoQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class MapsActivity extends AppCompatActivity implements LifecycleRegistryOwner, OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";

    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    private GoogleMap mMap = null;
    private final HashMap<String, TramMarker> mTramMarkerHashMap = new HashMap<>();

    private MenuItemRefreshCtrl mMenuItemRefresh = null;
    private MenuItem mMenuItemFavoriteSwitch = null;
    private AdView mAdView;

    private IconGenerator mIconGenerator;
    private final ArrayList<TramMarker> mAnimationMarkers = new ArrayList<>();
    private final ValueAnimator mValueAnimator = ValueAnimator
            .ofFloat(0, 1)
            .setDuration(3000);

    private MainActivityViewModel mViewModel;
    private LiveData<Boolean> mFavoriteView;
    private LiveData<Location> mLocationLiveData;
    private List<String> mFavoriteTrams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mViewModel.getTramData().observe(this, tramDataList -> {
            if (tramDataList == null)
                return;
            Toast.makeText(getApplicationContext(), "Aktualizacja pozycji pojazdów", Toast.LENGTH_SHORT).show();
            HashMap<String, TramData> tramDataHashMap = new HashMap<>();
            for (TramData tramData : tramDataList) {
                tramDataHashMap.put(tramData.getId(), tramData);
            }
            updateExistingMarkers(tramDataHashMap);
            if (tramDataHashMap.size() == mTramMarkerHashMap.size())
                return;
            addNewMarkers(tramDataHashMap);
        });

        mFavoriteView = mViewModel.isFavoriteView();
        mFavoriteView.observe(this, aBoolean -> {
            if (aBoolean != null && mMenuItemFavoriteSwitch != null)
                mMenuItemFavoriteSwitch.setIcon(
                        aBoolean ? R.drawable.fav_on : R.drawable.fav_off);
            if (aBoolean != null && mMap != null) {
                mMap.setMinZoomPreference(aBoolean ? 0 : 14.5f);
            }
            updateMarkersVisibility();
        });

        mViewModel.getFavoriteTramsLiveData().observe(this, strings -> {
            mFavoriteTrams = strings;
            updateMarkersVisibility();
        });

        mViewModel.getLoadingLiveData().observe(this, aBoolean -> {
            if (mMenuItemRefresh == null || aBoolean == null)
                return;
            if (aBoolean)
                mMenuItemRefresh.startAnimation();
            else
                mMenuItemRefresh.endAnimation();

        });

        if (checkLocationPermission()) {
            subscribeLocationLiveData();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    private void subscribeLocationLiveData() {
        mLocationLiveData = mViewModel.getLocationLiveData();
        mLocationLiveData.observe(this, location -> {
            if (location == null)
                return;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMap != null) {
                reloadAds(location);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                if (mLocationLiveData != null) {
                    mLocationLiveData.removeObservers(this);
                    mLocationLiveData = null;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null)
            mAdView.resume();
    }

    @Override
    protected void onPause() {
        if (mAdView != null)
            mAdView.pause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (menuItem != null)
            mMenuItemRefresh = new MenuItemRefreshCtrl(this, menuItem);

        mMenuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch);
        if (mFavoriteView != null)
           mMenuItemFavoriteSwitch.setIcon(
                   mFavoriteView.getValue() ? R.drawable.fav_on : R.drawable.fav_off);

        return super.onCreateOptionsMenu(menu);
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
            mViewModel.forceReload();
            return true;
        } else if (item.getItemId() == R.id.menu_item_favorite) {
            Intent intent = new Intent(this, FavoriteLinesActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_item_favorite_switch) {
            mViewModel.toggleFavorite();
        }
        return super.onOptionsItemSelected(item);
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

    private void updateMarkerVisibility(@NonNull TramMarker tramMarker) {
        tramMarker.setVisible(!mFavoriteView.getValue()
                || mFavoriteTrams.contains(tramMarker.getTramLine()));
    }

    @UiThread
    private void updateExistingMarkers(@NonNull HashMap<String, TramData> tramDataHashMap) {
        Iterator<Map.Entry<String, TramMarker>> iter = mTramMarkerHashMap.entrySet().iterator();
        mAnimationMarkers.clear();
        while (iter.hasNext()) {
            Map.Entry<String, TramMarker> element = iter.next();
            TramMarker tramMarker = element.getValue();
            TramData updatedData = tramDataHashMap.get(element.getKey());
            if (updatedData != null) {
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
    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (!mFavoriteView.getValue()) {
            mMap.setMinZoomPreference(14.5f);
        }
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)
                mMap.setMyLocationEnabled(true);
            else
                mMap.setMyLocationEnabled(false);
        } else
            mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        LatLng position;
        if (mLocationLiveData != null && mLocationLiveData.getValue() != null)
            position = new LatLng(
                    mLocationLiveData.getValue().getLatitude(),
                    mLocationLiveData.getValue().getLongitude());
        else
            position = new LatLng(52.231841, 21.005940);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
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
                .addTestDevice(getString(R.string.adMobTestDeviceS8Plus))
                .setLocation(location)
                .build();
        mAdView.loadAd(adRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    subscribeLocationLiveData();
                } else
                    mMap.setMyLocationEnabled(false);
            }
        }
    }

    @Override
    public LifecycleRegistry getLifecycle() {
        return mLifecycleRegistry;
    }
}
