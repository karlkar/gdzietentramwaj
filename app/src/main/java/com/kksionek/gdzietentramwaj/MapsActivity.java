package com.kksionek.gdzietentramwaj;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;

    private GoogleMap mMap;
    private Timer mTimer;
    IconGenerator mIconGenerator;
    private HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private HashMap<String, Pair<Marker, Polyline>> mTramMarkerHashMap = new HashMap<>();
    private LinearInterpolator mLatLngInterpolator = new LinearInterpolator();
    final Handler handler = new Handler();

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

        final View adBackground = findViewById(R.id.adBackground);
        AdView adView = (AdView) findViewById(R.id.adView);
        adBackground.setVisibility(View.GONE);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.adMobTestDeviceNote5))
                .addTestDevice(getString(R.string.adMobTestDeviceS5))
                .build();
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (Build.VERSION.SDK_INT >= 19)
                    TransitionManager.beginDelayedTransition((ViewGroup) findViewById(R.id.rootView));
                adBackground.setVisibility(View.VISIBLE);
            }
        });
        adView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new TramLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 1000, 30000);
    }

    @Override
    protected void onPause() {
        mTimer.cancel();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMarkers() {
        synchronized (mTramDataHashMap) {
            Toast.makeText(getApplicationContext(), "Aktualizacja pozycji", Toast.LENGTH_SHORT).show();
            for (final Map.Entry<String, TramData> element : mTramDataHashMap.entrySet()) {
                final LatLng newPosition = new LatLng(element.getValue().getLat(), element.getValue().getLon());
                if (mTramMarkerHashMap.containsKey(element.getKey())) {
                    final Marker marker = mTramMarkerHashMap.get(element.getKey()).first;
                    if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(marker.getPosition())
                            || mMap.getProjection().getVisibleRegion().latLngBounds.contains(newPosition)) {
                        final LatLng startPosition = marker.getPosition();
                        final long start = SystemClock.uptimeMillis();
                        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
                        final float durationInMs = 3000;

                        handler.post(new Runnable() {
                            long elapsed;
                            float t;
                            float v;

                            @Override
                            public void run() {
                                elapsed = SystemClock.uptimeMillis() - start;
                                t = elapsed / durationInMs;
                                v = interpolator.getInterpolation(t);

                                LatLng intermediatePosition = mLatLngInterpolator.interpolate(v, startPosition, newPosition);
                                marker.setPosition(intermediatePosition);
                                Polyline polyline = mTramMarkerHashMap.get(element.getKey()).second;
                                List<LatLng> points = polyline.getPoints();
                                if (points.size() > 100)
                                    points.remove(points.get(0));

                                points.add(intermediatePosition);
                                polyline.setPoints(points);

                                if (t < 1)
                                    handler.postDelayed(this, 16);
                            }
                        });
                    } else {
                        marker.setPosition(newPosition);
                        Polyline polyline = mTramMarkerHashMap.get(element.getKey()).second;
                        List<LatLng> points = polyline.getPoints();
                        points.add(newPosition);
                        polyline.setPoints(points);
                    }
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(
                            mIconGenerator.makeIcon(element.getValue().getFirstLine()))).position(newPosition).title(element.getValue().getFirstLine()));
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 13));
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mMap.setOnMyLocationChangeListener(null);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
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

    class TramLoader extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String response = null;
            try {
                URL url = new URL("https://api.um.warszawa.pl/api/action/wsstore_get/?id=c7238cfe-8b1f-4c38-bb4a-de386db7e776&apikey=***REMOVED***");
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
                Log.d("MAPSACTIVITY", "doInBackground: response is null, sorry");
                return false;
            } else {
                Log.d("MAPSACTIVITY", "doInBackground: parsing response");
                synchronized (mTramDataHashMap) {
                    HashMap<String, TramData> map = new HashMap<>();
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONArray array = jsonObject.getJSONArray("result");
                        for (int i = 0; i < array.length(); ++i) {
                            TramData data = new TramData(array.getJSONObject(i));
                            map.put(data.getId(), data);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
                return true;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result)
                updateMarkers();
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
