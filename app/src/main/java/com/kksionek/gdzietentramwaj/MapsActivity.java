package com.kksionek.gdzietentramwaj;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";

    private final Model mModel = Model.getInstance();

    private GoogleMap mMap = null;
    private final HashMap<String, TramMarker> mTramMarkerHashMap = new HashMap<>();
    private final HashMap<Marker, String> mMarkerTramIdMap = new HashMap<>();
    private boolean mFavoriteView;

    private MenuItemRefreshCtrl mMenuItemRefresh = null;
    private MenuItem mMenuItemFavoriteSwitch = null;
    private final Handler mAnimHandler = new Handler();
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mModel.setObserver(this);
        PrefManager.init(this);
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
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceNote5))
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .setLocation(loc)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null)
            mModel.startUpdates();
        if (mModel.getFavoriteManager().checkIfChangedAndReset())
            updateMarkersVisibility();
    }

    @Override
    protected void onPause() {
        mModel.stopUpdates();
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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

        mMenuItemFavoriteSwitch = menu.findItem(R.id.menu_item_favorite_switch);
        updateFavoriteSwitchIcon();

        return super.onCreateOptionsMenu(menu);
    }

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

    private void updateMarkersVisibility() {
        for (TramMarker tramMarker : mTramMarkerHashMap.values())
            tramMarker.setVisible(!mFavoriteView || mModel.getFavoriteManager().isFavorite(tramMarker.getTramLine()));
    }

    public void updateMarkers(HashMap<String, TramData> tramDataHashMap) {
        Toast.makeText(getApplicationContext(), "Aktualizacja pozycji tramwajów", Toast.LENGTH_SHORT).show();

        updateExistingMarkers(tramDataHashMap);

        if (tramDataHashMap.size() == mTramMarkerHashMap.size())
            return;

        addNewMarkers(tramDataHashMap);
    }

    @UiThread
    private void updateExistingMarkers(HashMap<String, TramData> tramDataHashMap) {
        Iterator<Map.Entry<String, TramMarker>> iter = mTramMarkerHashMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, TramMarker> element = iter.next();
            TramMarker tramMarker = element.getValue();
            if (tramDataHashMap.containsKey(element.getKey())) {
                TramData updatedData = tramDataHashMap.get(element.getKey());

                LatLng prevPosition = updatedData.getPrevLatLng();
                LatLng newPosition = updatedData.getLatLng();
                if (prevPosition.equals(newPosition))
                    continue;

                if (shouldAnimateMarkerMovement(tramMarker, newPosition))
                    tramMarker.animateMovement(newPosition, mAnimHandler);
                else
                    tramMarker.updateMarker(prevPosition, newPosition);
            } else {
                mMarkerTramIdMap.remove(tramMarker.getMarker());
                tramMarker.remove();
                iter.remove();
            }
        }
    }

    private boolean shouldAnimateMarkerMovement(TramMarker tramMarker, LatLng newPosition) {
        return tramMarker.isVisible() &&
                (mMap.getProjection().getVisibleRegion().latLngBounds.contains(tramMarker.getMarkerPosition())
                        || mMap.getProjection().getVisibleRegion().latLngBounds.contains(newPosition));
    }

    private void addNewMarkers(HashMap<String, TramData> tramDataHashMap) {
        for (Map.Entry<String, TramData> element : tramDataHashMap.entrySet()) {
            if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                TramMarker tramMarker = new TramMarker(this, element.getValue(), mMap);
                tramMarker.setVisible(!mFavoriteView || mModel.getFavoriteManager().isFavorite(tramMarker.getTramLine()));
                mTramMarkerHashMap.put(element.getKey(), tramMarker);
                mMarkerTramIdMap.put(tramMarker.getMarker(), element.getKey());
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            mMap.setMyLocationEnabled(
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        else
            mMap.setMyLocationEnabled(true);
        mMap.setTrafficEnabled(false);
        LatLng warsaw = new LatLng(52.231841, 21.005940);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 15));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(getString(R.string.adMobTestDeviceNote5))
                        .addTestDevice(getString(R.string.adMobTestDeviceS5))
                        .setLocation(location)
                        .build();
                mAdView.loadAd(adRequest);

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

        mModel.startUpdates();
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
