package com.kksionek.gdzietentramwaj;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.drive.realtime.internal.event.ObjectChangedDetails;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;
    private static final String TAG = "MAPSACTIVITY";
    private static final String WARSZAWA_TRAM_API = "https://api.um.warszawa.pl/api/action/wsstore_get/?id=c7238cfe-8b1f-4c38-bb4a-de386db7e776&apikey=***REMOVED***";

    private GoogleMap mMap;
    private Timer mTimer;
    private IconGenerator mIconGenerator;
    private HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private HashMap<String, Pair<Marker, Polyline>> mTramMarkerHashMap = new HashMap<>();
    private final LinearInterpolator mLatLngInterpolator = new LinearInterpolator();
    private final Handler handler = new Handler();

    private final Object mDataLoaderMutex = new Object();
    private AsyncTask<Void, Void, Boolean> mDataLoader = null;

    private MenuItem mMenuItem = null;
    private Animation mRotationAnimation = null;
    private ImageView mRefreshImage = null;

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
        scheduleRefresh();
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    private void startUpdateAnim() {
        if (mMenuItem == null)
            return;
        if (mRefreshImage == null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRefreshImage = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
        }
        if (mRotationAnimation == null)
            mRotationAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_rotate);
        if (mMenuItem.getActionView() == null) {
            mRefreshImage.startAnimation(mRotationAnimation);
            mMenuItem.setActionView(mRefreshImage);
            mMenuItem.setEnabled(false);
        }
    }

    private void stopUpdateAnim() {
        if (mMenuItem == null)
            return;
        if (mMenuItem.getActionView() != null) {
            mMenuItem.getActionView().clearAnimation();
            mMenuItem.setActionView(null);
        }
        mMenuItem.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuItem = menu.findItem(R.id.menu_item_refresh);
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
            scheduleRefresh();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMarkers() {
        synchronized (mTramDataHashMap) {
            Toast.makeText(getApplicationContext(), "Aktualizacja pozycji tramwajów", Toast.LENGTH_SHORT).show();
            Iterator<Map.Entry<String, Pair<Marker, Polyline>>> iter = mTramMarkerHashMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, Pair<Marker, Polyline>> element = iter.next();
                final Marker marker = element.getValue().first;
                final Polyline polyline = element.getValue().second;
                if (mTramDataHashMap.containsKey(element.getKey())) {
                    TramData updatedData = mTramDataHashMap.get(element.getKey());
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
                    marker.remove();
                    polyline.remove();
                    iter.remove();
                }
            }

            if (mTramDataHashMap.size() == mTramMarkerHashMap.size())
                return;

            for (Map.Entry<String, TramData> element : mTramDataHashMap.entrySet()) {
                if (!mTramMarkerHashMap.containsKey(element.getKey())) {
                    LatLng newPosition = element.getValue().getLatLng();
                    Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(
                            mIconGenerator.makeIcon(element.getValue().getFirstLine()))).position(newPosition));
                    Polyline polyline = mMap.addPolyline(new PolylineOptions().add(newPosition).width(5));
                    mTramMarkerHashMap.put(element.getKey(), new Pair<>(marker, polyline));
                }
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

    private void scheduleRefresh() {
        Log.d(TAG, "scheduleRefresh: START");
        if (mTimer != null)
            mTimer.cancel();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mDataLoaderMutex) {
                    if (mDataLoader == null) {
                        mDataLoader = new TramLoader();
                        mDataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            }
        }, 10, 30000);
    }

    class TramLoader extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startUpdateAnim();
                }
            });
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = null;
            try {
                URL url = new URL(WARSZAWA_TRAM_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() < 300 && conn.getResponseCode() >= 200) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String str;
                    StringBuffer stringBuffer = new StringBuffer();
                    while ((str = bufferedReader.readLine()) != null)
                        stringBuffer.append(str);
                    response = stringBuffer.toString();
                }
                conn.disconnect();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }

            if (response == null) {
                Log.d(TAG, "doInBackground: response is null, sorry");
                return false;
            } else {
                Log.d(TAG, "doInBackground: parsing response");
                synchronized (mTramDataHashMap) {
                    HashMap<String, TramData> map = new HashMap<>();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.optJSONArray("result");
                        if (array == null) {
                            JSONObject errorObject = jsonObject.getJSONObject("result");
                            Log.d(TAG, "doInBackground: Error occurred: '" + errorObject.getString("Message") + "'");
                            return false;
                        }
                        for (int i = 0; i < array.length(); ++i) {
                            TramData data = new TramData(array.getJSONObject(i));
                            if (data.isRunning())
                                map.put(data.getId(), data);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return false;
                    }

                    if (map.size() == 0) {
                        Log.d(TAG, "Received empty response = '" + response + "'");
                        return false;
                    }

                    // update model
                    Iterator<Map.Entry<String, TramData>> iter = mTramDataHashMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry<String, TramData> tmp = iter.next();
                        if (!map.containsKey(tmp.getValue().getId()))
                            iter.remove();
                    }
                    for (TramData tramData : map.values()) {
                        if (mTramDataHashMap.containsKey(tramData.getId()))
                            mTramDataHashMap.get(tramData.getId()).updatePosition(tramData);
                        else
                            mTramDataHashMap.put(tramData.getId(), tramData);
                    }
                }
                Log.d(TAG, "doInBackground: parsing done");
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            synchronized (mDataLoaderMutex) {
                mDataLoader = null;
            }
            if (result) {
                stopUpdateAnim();
                updateMarkers();
            } else
                scheduleRefresh();
        }
    }

    public class LinearInterpolator {
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lng = (b.longitude - a.longitude) * fraction + a.longitude;
            return new LatLng(lat, lng);
        }
    }
}
