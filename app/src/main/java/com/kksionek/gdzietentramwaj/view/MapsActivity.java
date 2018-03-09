package com.kksionek.gdzietentramwaj.view;

import android.Manifest;
import android.animation.ValueAnimator;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.ViewModel.MainActivityViewModel;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";
    private static final int MAX_VISIBLE_MARKERS = 50;
    private static final double DISTANCE = 0.1 / 6371.0;

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
    private AtomicBoolean mCameraMoveInProgress = new AtomicBoolean(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mViewModel.getTramData().observe(this, tramDataWrapper -> {
            if (tramDataWrapper == null
                    || tramDataWrapper.throwable instanceof UnknownHostException) {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.error_internet,
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (tramDataWrapper.throwable != null) {
                Toast.makeText(
                        getApplicationContext(),
                        R.string.error_ztm,
                        Toast.LENGTH_LONG).show();
                return;
            }
            Map<String, TramData> tramDataHashMap = tramDataWrapper.tramDataHashMap;
            Toast.makeText(
                    getApplicationContext(),
                    R.string.position_update_sucessful,
//                    "Zaktualizowano pozycję " + tramDataHashMap.size() + " pojazdów.",
                    Toast.LENGTH_SHORT).show();
            updateExistingMarkers(tramDataHashMap);
            if (tramDataHashMap.size() == mTramMarkerHashMap.size()) {
                return;
            }
            addNewMarkers(tramDataHashMap);
        });

        mFavoriteView = mViewModel.isFavoriteView();
        mFavoriteView.observe(this, aBoolean -> {
            if (aBoolean != null && mMenuItemFavoriteSwitch != null) {
                mMenuItemFavoriteSwitch.setIcon(
                        aBoolean ? R.drawable.fav_on : R.drawable.fav_off);
            }
            updateMarkersVisibility();
        });

        mViewModel.getFavoriteTramsLiveData().observe(this, strings -> {
            mFavoriteTrams = strings;
            updateMarkersVisibility();
        });

        mViewModel.getLoadingLiveData().observe(this, aBoolean -> {
            if (mMenuItemRefresh == null || aBoolean == null) {
                return;
            }
            if (aBoolean) {
                mMenuItemRefresh.startAnimation();
            } else {
                mMenuItemRefresh.endAnimation();
            }
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
        mAdView = findViewById(R.id.adView);
        reloadAds(loc);

        mIconGenerator = new IconGenerator(this);
        mValueAnimator.addUpdateListener(animation -> {
            LatLng intermediatePos;
            float fraction = animation.getAnimatedFraction();
            for (TramMarker tramMarker : mAnimationMarkers) {
                if (tramMarker.getMarker() == null) {
                    continue;
                }
                intermediatePos = SphericalUtil.interpolate(
                        tramMarker.getPrevPosition(),
                        tramMarker.getFinalPosition(),
                        fraction);
                tramMarker.getMarker().setPosition(intermediatePos);

                List<LatLng> pointsList = new ArrayList<>();
                LatLng prevPoint;
                if (tramMarker.getPolyline().getPoints().size() != 0) {
                    prevPoint = tramMarker.getPolyline().getPoints().get(0);
                } else {
                    prevPoint = tramMarker.getPrevPosition();
                }
                if (SphericalUtil.computeDistanceBetween(
                        intermediatePos,
                        prevPoint) < 100) {
                    pointsList.add(tramMarker.getPrevPosition());
                    pointsList.add(intermediatePos);
                } else {
                    pointsList.add(computeDistanceAndBearing(
                            intermediatePos,
                            prevPoint));
                    pointsList.add(intermediatePos);
                }
                tramMarker.getPolyline().setPoints(pointsList);
            }
        });
    }

    private static LatLng computeDistanceAndBearing(LatLng dst, LatLng src) {
        double brng = Math.toRadians(bearing(dst, src));
        double lat1 = Math.toRadians(dst.latitude);
        double lon1 = Math.toRadians(dst.longitude);

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(DISTANCE) + Math.cos(lat1)*Math.sin(DISTANCE)*Math.cos(brng) );
        double a = Math.atan2(Math.sin(brng)*Math.sin(DISTANCE)*Math.cos(lat1), Math.cos(DISTANCE)-Math.sin(lat1)*Math.sin(lat2));
        double lon2 = lon1 + a;

        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI;
        return new LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    private static double bearing(LatLng src, LatLng dst) {
        double degToRad = Math.PI / 180.0;
        double phi1 = src.latitude * degToRad;
        double phi2 = dst.latitude * degToRad;
        double lam1 = src.longitude * degToRad;
        double lam2 = dst.longitude * degToRad;

        return Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                          Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)
        ) * 180/Math.PI;
    }

    private void subscribeLocationLiveData() {
        mLocationLiveData = mViewModel.getLocationLiveData();
        mLocationLiveData.observe(this, location -> {
            if (location == null) {
                return;
            }
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
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (menuItem != null) {
            mMenuItemRefresh = new MenuItemRefreshCtrl(this, menuItem);
        }

        mMenuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch);
        if (mFavoriteView != null) {
            mMenuItemFavoriteSwitch.setIcon(
                    mFavoriteView.getValue() ? R.drawable.fav_on : R.drawable.fav_off);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_info) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.about_app);
            builder.setMessage(R.string.disclaimer);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
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
            marker.setFavoriteVisible(!mFavoriteView.getValue()
                    || mFavoriteTrams.contains(marker.getTramLine()));
        }
        showOrZoom();
    }

    @UiThread
    private void updateExistingMarkers(@NonNull Map<String, TramData> tramDataHashMap) {
        Iterator<Map.Entry<String, TramMarker>> iter = mTramMarkerHashMap.entrySet().iterator();
        mAnimationMarkers.clear();
        while (iter.hasNext()) {
            Map.Entry<String, TramMarker> element = iter.next();
            TramMarker tramMarker = element.getValue();
            TramData updatedData = tramDataHashMap.get(element.getKey());
            if (updatedData != null) {
                LatLng prevPosition = tramMarker.getFinalPosition();
                LatLng newPosition = updatedData.getLatLng();
                if (prevPosition.equals(newPosition)) {
                    continue;
                }

                tramMarker.updatePosition(newPosition);

                if (!mCameraMoveInProgress.get() && tramMarker.isFavoriteVisible()) {
                    if (tramMarker.isOnMap(mMap)) {
                        if (tramMarker.getMarker() == null) {
                            Marker m = mMap.addMarker(new MarkerOptions()
                                    .position(tramMarker.getPrevPosition())
                                    .icon(TramMarker.getBitmap(
                                            tramMarker.getTramLine(),
                                            mIconGenerator)));
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
                    } else {
                        tramMarker.remove();
                    }
                }
            } else {
                tramMarker.remove();
                iter.remove();
            }
        }
        if (!mAnimationMarkers.isEmpty()) {
            mValueAnimator.start();
        }
    }

    @UiThread
    private void addNewMarkers(@NonNull Map<String, TramData> tramDataHashMap) {
        for (Map.Entry<String, TramData> element : tramDataHashMap.entrySet()) {
            if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                TramMarker marker = new TramMarker(element.getValue());
                mTramMarkerHashMap.put(element.getKey(), marker);
                marker.setFavoriteVisible(!mFavoriteView.getValue()
                        || mFavoriteTrams.contains(marker.getTramLine()));
                if (!mCameraMoveInProgress.get()
                        && marker.isFavoriteVisible()
                        && marker.isOnMap(mMap)) {
                    createNewFullMarker(marker);
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
            List<LatLng> pointsList = new ArrayList<>();
            if (SphericalUtil.computeDistanceBetween(
                    tramMarker.getFinalPosition(),
                    tramMarker.getPrevPosition()) < 100) {
                pointsList.add(tramMarker.getPrevPosition());
                pointsList.add(tramMarker.getFinalPosition());
            } else {
                pointsList.add(computeDistanceAndBearing(
                        tramMarker.getFinalPosition(),
                        tramMarker.getPrevPosition()));
                pointsList.add(tramMarker.getFinalPosition());
            }

            Polyline p = mMap.addPolyline(new PolylineOptions()
                    .color(Color.argb(255, 236, 57, 57))
                    .width(TramMarker.POLYLINE_WIDTH));
            p.setPoints(pointsList);
            tramMarker.setPolyline(p);
        }
    }

    @UiThread
    private boolean checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

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
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
            }
        } else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setTrafficEnabled(false);
        LatLng position;
        if (mLocationLiveData != null && mLocationLiveData.getValue() != null) {
            position = new LatLng(
                    mLocationLiveData.getValue().getLatitude(),
                    mLocationLiveData.getValue().getLongitude());
        } else {
            position = new LatLng(52.231841, 21.005940);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        mMap.setOnMarkerClickListener(marker -> true);
        mMap.setOnCameraMoveStartedListener(i -> mCameraMoveInProgress.set(true));
        mMap.setOnCameraIdleListener(() -> {
            mCameraMoveInProgress.set(false);
            showOrZoom();
        });
    }

    private void showOrZoom() {
        ArrayList<TramMarker> markersToBeCreated = new ArrayList<>();
        for (TramMarker marker : mTramMarkerHashMap.values()) {
            if (marker.isFavoriteVisible() && marker.isOnMap(mMap)) {
                markersToBeCreated.add(marker);
            } else {
                marker.remove();
            }
        }
        if (markersToBeCreated.size() <= MAX_VISIBLE_MARKERS) {
            for (TramMarker marker : markersToBeCreated) {
                createNewFullMarker(marker);
            }
        } else {
            mMap.animateCamera(CameraUpdateFactory.zoomIn());
        }
    }

    private void reloadAds(Location location) {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .addTestDevice(getString(R.string.adMobTestDeviceS7))
                .addTestDevice(getString(R.string.adMobTestDeviceS9plus))
                .setLocation(location)
                .build();
        mAdView.loadAd(adRequest);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    subscribeLocationLiveData();
                } else {
                    mMap.setMyLocationEnabled(false);
                }
            }
        }
    }
}
