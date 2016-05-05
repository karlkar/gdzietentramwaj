package com.kksionek.gdzietentramwaj;


import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

class TramLoader extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "TRAMLOADER";
    private final String mAddress;
    private final Model mModel;
    private boolean mDone = false;

    public TramLoader(String address, Model model) {
        mAddress = address;
        mModel = model;
    }

    @UiThread
    public void launch() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public boolean isDone() {
        return mDone;
    }

    @Override
    protected void onPreExecute() {
        mModel.notifyRefreshStarted();
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        String response = null;
        try {
            URL url = new URL(mAddress);
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
                    if (data.shouldBeVisible())
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

            mModel.update(map);
            Log.d(TAG, "doInBackground: parsing done");
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDone = true;
        mModel.notifyJobDone(result);
    }
}