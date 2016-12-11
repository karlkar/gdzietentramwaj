package com.kksionek.gdzietentramwaj;

import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;

import retrofit2.Response;

class TramLoader extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = "TRAMLOADER";
    private final String mAddress;
    private final Model mModel;
    private boolean mDone = false;
    private TramInterface mTramInterface;

    public TramLoader(String address, Model model, TramInterface tramInterface) {
        mAddress = address;
        mModel = model;
        mTramInterface = tramInterface;
    }

    @UiThread
    public void launch() {
        mDone = false;
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
        if (isCancelled())
            return false;

        Response<TramList> tramListResponse = null;
        try {
            tramListResponse = mTramInterface.getTrams(TramInterface.ID, TramInterface.APIKEY).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tramListResponse == null) {
            Log.d(TAG, "doInBackground: response is null, sorry");
            return false;
        } else {
            Log.d(TAG, "doInBackground: parsing response");
            HashMap<String, TramData> map = new HashMap<>();
            for (TramData data : tramListResponse.body().getList()) {
                if (data.shouldBeVisible())
                    map.put(data.getId(), data);
            }

            if (map.size() == 0)
                return false;

            mModel.update(map);
            Log.d(TAG, "doInBackground: parsing done");
            return true;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDone = true;
        if (isCancelled())
            return;
        mModel.notifyJobDone(result);
    }
}