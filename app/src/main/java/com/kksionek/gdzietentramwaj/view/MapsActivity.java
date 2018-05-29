package com.kksionek.gdzietentramwaj.view;

import android.Manifest;
import android.animation.ValueAnimator;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.JsonSyntaxException;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;
import com.kksionek.gdzietentramwaj.BuildConfig;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.R;
import com.kksionek.gdzietentramwaj.TramApplication;
import com.kksionek.gdzietentramwaj.ViewModel.MainActivityViewModel;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MAPSACTIVITY";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;

    private static final int MAX_VISIBLE_MARKERS = 50;
    private static final double MAX_DISTANCE = 150;
    private static final double MAX_DISTANCE_RAD = MAX_DISTANCE / 6371000;

    private static final int BUILD_VERSION_WELCOME_WINDOW_ADDED = 23;
    private static final String PREF_LAST_VERSION = "LAST_VERSION";

    private GoogleMap mMap = null;
    private final HashMap<String, TramMarker> mTramMarkerHashMap = new HashMap<>();

    private MenuItemRefreshCtrl mMenuItemRefresh = null;
    private MenuItem mMenuItemFavoriteSwitch = null;
    @Inject
    AdProviderInterface adProviderInterface;


    private IconGenerator mIconGenerator;
    private final ArrayList<TramMarker> mAnimationMarkers = new ArrayList<>();
    private final ValueAnimator mValueAnimator = ValueAnimator
            .ofFloat(0, 1)
            .setDuration(3000);

    private MainActivityViewModel mViewModel;
    private LiveData<Boolean> mFavoriteView;
    private List<String> mFavoriteTrams;
    private AtomicBoolean mCameraMoveInProgress = new AtomicBoolean(false);

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = animation -> {
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

            LatLng prevPos;
            if (tramMarker.getPolyline().getPoints().size() != 0) {
                prevPos = tramMarker.getPolyline().getPoints().get(0);
            } else {
                prevPos = tramMarker.getPrevPosition();
            }
            List<LatLng> pointsList = generatePolylinePoints(intermediatePos, prevPos);
            tramMarker.getPolyline().setPoints(pointsList);
        }
    };

    private final Observer<TramDataWrapper> mTramDataObserver = tramDataWrapper -> {
        if (tramDataWrapper == null
                || tramDataWrapper.throwable instanceof UnknownHostException
                || tramDataWrapper.throwable instanceof SocketTimeoutException) {
            Toast.makeText(
                    this,
                    R.string.error_internet,
                    Toast.LENGTH_LONG).show();
            return;
        }
        if (tramDataWrapper.throwable != null) {
            if (!BuildConfig.DEBUG
                    && !(tramDataWrapper.throwable instanceof JsonSyntaxException)
                    && !(tramDataWrapper.throwable instanceof IllegalStateException)
                    && !(tramDataWrapper.throwable instanceof HttpException)) {
                Crashlytics.log("Error handled with a toast.");
                Crashlytics.logException(tramDataWrapper.throwable);
            }
            Toast.makeText(
                    this,
                    BuildConfig.DEBUG ?
                            tramDataWrapper.throwable.getClass().getSimpleName() + ": " + tramDataWrapper.throwable.getMessage()
                            : getString(R.string.error_ztm),
                    Toast.LENGTH_LONG).show();
            return;
        }

        if (mMenuItemRefresh != null) {
            if (tramDataWrapper.loading) {
                mMenuItemRefresh.startAnimation();
            } else {
                mMenuItemRefresh.endAnimation();
            }
        }

        if (tramDataWrapper.tramDataHashMap == null) {
            return;
        }

        if (tramDataWrapper.tramDataHashMap.size() == 0) {
            Toast.makeText(
                    this,
                    R.string.none_position_is_up_to_date,
                    Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, TramData> tramDataHashMap = tramDataWrapper.tramDataHashMap;
        Toast.makeText(
                getApplicationContext(),
                (BuildConfig.DEBUG ?
                        getString(
                                R.string.position_update_successful_amount,
                                tramDataHashMap.size())
                        : getString(R.string.position_update_sucessful)),
                Toast.LENGTH_SHORT).show();
        updateExistingMarkers(tramDataHashMap);
        addNewMarkers(tramDataHashMap);
    };

    private final Observer<Boolean> mFavoriteModeObserver = it -> {
        if (it != null && mMenuItemFavoriteSwitch != null) {
            mMenuItemFavoriteSwitch.setIcon(
                    it ? R.drawable.fav_on : R.drawable.fav_off);
        }
        updateMarkersVisibility();
    };

    private Observer<List<String>> mFavoriteTramsObserver = strings -> {
        mFavoriteTrams = strings;
        updateMarkersVisibility();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        TramApplication.getAppComponent().inject(this);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        mViewModel.getTramData().observe(this, mTramDataObserver);
        mViewModel.getFavoriteTramsLiveData().observe(this, mFavoriteTramsObserver);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        adProviderInterface.showAd(findViewById(R.id.adView));
        if (checkLocationPermission(true)) {
            applyLastKnownLocation(true, false);
        } else {
            reloadAds(null);
        }

        mIconGenerator = new IconGenerator(this);
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);

        handleWelcomeDialog();
    }

    private void handleWelcomeDialog() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int lastVersion = sharedPreferences.getInt(PREF_LAST_VERSION, 0);
        if (lastVersion < BUILD_VERSION_WELCOME_WINDOW_ADDED) {
            showAboutAppDialog();
        }
        sharedPreferences.edit().putInt(PREF_LAST_VERSION, BuildConfig.VERSION_CODE).apply();
    }

    private void applyLastKnownLocation(boolean adView, boolean map) {
        mViewModel.getLastKnownLocation().addOnSuccessListener(
                this,
                location -> {
                    if (location != null) {
                        if (adView) {
                            reloadAds(location);
                        }
                        if (map && mMap != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        adProviderInterface.resume();
    }

    @Override
    protected void onPause() {
        adProviderInterface.pause();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_item_refresh);
        if (menuItem != null) {
            mMenuItemRefresh = new MenuItemRefreshCtrl(this, menuItem);
        }

        //noinspection ConstantConditions
        if (BuildConfig.FLAVOR.equals("paid")) {
            MenuItem removeAds = menu.findItem(R.id.menu_item_remove_ads);
            removeAds.setVisible(false);
        }

        mMenuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch);
        mFavoriteView = mViewModel.isFavoriteView();
        mFavoriteView.observe(this, mFavoriteModeObserver);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_info) {
            showAboutAppDialog();
            return true;
        } else if (item.getItemId() == R.id.menu_item_refresh) {
            mViewModel.forceReload();
            return true;
        } else if (item.getItemId() == R.id.menu_item_remove_ads) {
            removeAds();
        } else if (item.getItemId() == R.id.menu_item_rate) {
            rateApp();
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
        boolean onlyFavorites;
        if (this.mFavoriteView == null || this.mFavoriteView.getValue() == null) {
            onlyFavorites = false;
        } else {
            onlyFavorites = this.mFavoriteView.getValue();
        }
        for (TramMarker marker : mTramMarkerHashMap.values()) {
            marker.setFavoriteVisible(!onlyFavorites
                    || mFavoriteTrams.contains(marker.getTramLine()));
        }
        showOrZoom();
    }

    @UiThread
    private void updateExistingMarkers(@NonNull Map<String, TramData> tramDataHashMap) {
        LatLngBounds visibleRegion = null;
        if (mMap != null) {
            visibleRegion = mMap.getProjection().getVisibleRegion().latLngBounds;
        }
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
                    if (tramMarker.isOnMap(visibleRegion)) {
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
        LatLngBounds visibleRegion = null;
        if (mMap != null) {
            visibleRegion = mMap.getProjection().getVisibleRegion().latLngBounds;
        }
        boolean onlyFavorites;
        if (this.mFavoriteView == null || this.mFavoriteView.getValue() == null) {
            onlyFavorites = false;
        } else {
            onlyFavorites = this.mFavoriteView.getValue();
        }
        for (Map.Entry<String, TramData> element : tramDataHashMap.entrySet()) {
            if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                TramMarker marker = new TramMarker(element.getValue());
                mTramMarkerHashMap.put(element.getKey(), marker);
                marker.setFavoriteVisible(!onlyFavorites
                        || mFavoriteTrams.contains(marker.getTramLine()));
                if (!mCameraMoveInProgress.get()
                        && marker.isFavoriteVisible()
                        && marker.isOnMap(visibleRegion)) {
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
            LatLng finalPosition = tramMarker.getFinalPosition();
            LatLng prevPosition = tramMarker.getPrevPosition();
            List<LatLng> pointsList = generatePolylinePoints(finalPosition, prevPosition);

            Polyline p = mMap.addPolyline(new PolylineOptions()
                    .color(Color.argb(255, 236, 57, 57))
                    .width(TramMarker.POLYLINE_WIDTH));
            p.setPoints(pointsList);
            tramMarker.setPolyline(p);
        }
    }

    @NonNull
    private static List<LatLng> generatePolylinePoints(LatLng finalPosition, LatLng prevPosition) {
        List<LatLng> pointsList = new ArrayList<>();
        if (finalPosition == null) {
            if (prevPosition == null) {
                return pointsList;
            } else {
                pointsList.add(prevPosition);
                return pointsList;
            }
        } else if (prevPosition == null) {
            pointsList.add(finalPosition);
            return pointsList;
        }

        if (SphericalUtil.computeDistanceBetween(
                finalPosition,
                prevPosition) < MAX_DISTANCE) {
            pointsList.add(prevPosition);
            pointsList.add(finalPosition);
        } else {
            pointsList.add(computePointInDirection(
                    finalPosition,
                    prevPosition));
            pointsList.add(finalPosition);
        }
        return pointsList;
    }

    private static LatLng computePointInDirection(LatLng dst, LatLng src) {
        double brng = Math.toRadians(bearing(dst, src));
        double lat1 = Math.toRadians(dst.latitude);
        double lon1 = Math.toRadians(dst.longitude);

        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(MAX_DISTANCE_RAD) + Math.cos(lat1)*Math.sin(MAX_DISTANCE_RAD)*Math.cos(brng) );
        double a = Math.atan2(Math.sin(brng)*Math.sin(MAX_DISTANCE_RAD)*Math.cos(lat1), Math.cos(MAX_DISTANCE_RAD)-Math.sin(lat1)*Math.sin(lat2));
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

    @UiThread
    private boolean checkLocationPermission(boolean doRequest) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (doRequest) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(52.231841, 21.005940), 15));
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        mMap.getUiSettings().setTiltGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setBuildingsEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
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

        if (checkLocationPermission(false)) {
            applyLastKnownLocation(false, true);
        }
        mMap.setOnMarkerClickListener(marker -> true);
        mMap.setOnCameraMoveStartedListener(i -> mCameraMoveInProgress.set(true));
        mMap.setOnCameraIdleListener(() -> {
            mCameraMoveInProgress.set(false);
            showOrZoom();
        });
    }

    private void showOrZoom() {
        if (mMap == null) {
            return;
        }
        ArrayList<TramMarker> markersToBeCreated = new ArrayList<>();
        LatLngBounds visibleRegion = mMap.getProjection().getVisibleRegion().latLngBounds;
        for (TramMarker marker : mTramMarkerHashMap.values()) {
            if (marker.isFavoriteVisible() && marker.isOnMap(visibleRegion)) {
                markersToBeCreated.add(marker);
                if (markersToBeCreated.size() > MAX_VISIBLE_MARKERS) {
                    mMap.animateCamera(CameraUpdateFactory.zoomIn());
                    return;
                }
            } else {
                marker.remove();
            }
        }
        for (TramMarker marker : markersToBeCreated) {
            createNewFullMarker(marker);
        }
    }

    private void reloadAds(Location location) {
        if (location == null) {
            location = new Location("");
            location.setLatitude(52.231841);
            location.setLongitude(21.005940);
        }
        adProviderInterface.loadAd(this, location);
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
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(true);
                    }
                    applyLastKnownLocation(true, true);
                } else {
                    if (mMap != null) {
                        mMap.setMyLocationEnabled(false);
                    }
                }
            }
        }
    }

    private void showAboutAppDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView text = new TextView(this);
        text.setText(R.string.disclaimer);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginSize = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.leftMargin = marginSize;
        params.rightMargin = marginSize;
        params.topMargin = marginSize;
        params.bottomMargin = marginSize;
        text.setLayoutParams(params);
        container.addView(text);

        builder.setTitle(R.string.about_app);
        builder.setView(container);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void removeAds() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        TextView text = new TextView(this);
        text.setText(R.string.remove_info);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int marginSize = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.leftMargin = marginSize;
        params.rightMargin = marginSize;
        params.topMargin = marginSize;
        params.bottomMargin = marginSize;
        text.setLayoutParams(params);
        container.addView(text);

        builder.setTitle(R.string.remove_title);
        builder.setView(container);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }
}
