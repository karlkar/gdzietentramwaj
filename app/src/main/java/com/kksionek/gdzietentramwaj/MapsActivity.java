package com.kksionek.gdzietentramwaj;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1234;

    private GoogleMap mMap;
    private Timer mTimer;
    private HashMap<String, TramData> mTramDataHashMap = new HashMap<>();
    private HashMap<String, Marker> mTramMarkerHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new TramLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }, 0, 30000);

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

    private void updateMarkers() {
        IconGenerator iconGenerator = new IconGenerator(this);
        synchronized (mTramDataHashMap) {
            Toast.makeText(getApplicationContext(), "UPDATE", Toast.LENGTH_SHORT).show();
            for (Map.Entry<String, TramData> element : mTramDataHashMap.entrySet()) {
                LatLng position = new LatLng(element.getValue().getLat(), element.getValue().getLon());
                if (mTramMarkerHashMap.containsKey(element.getKey())) {
                    mTramMarkerHashMap.get(element.getKey()).setPosition(position);
                } else {
                    Marker marker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(element.getValue().getFirstLine()))).position(position).title(element.getValue().getFirstLine()));
                    mTramMarkerHashMap.put(element.getKey(), marker);
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
}
