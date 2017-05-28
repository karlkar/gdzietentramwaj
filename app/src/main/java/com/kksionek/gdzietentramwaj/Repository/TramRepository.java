package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;

import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.util.List;

import javax.inject.Inject;

public class TramRepository {
    private static final String TAG = "TramRepository";

    private final TramLiveData mTramLiveData;
    private final TramDao mTramDao;

    @Inject
    public TramRepository(TramInterface tramInterface, TramDao tramDao) {
        mTramLiveData = new TramLiveData(tramInterface);
        mTramDao = tramDao;
    }

    public LiveData<List<TramData>> getDataStream() {
        return mTramLiveData;
    }

    public void forceReload() {
        mTramLiveData.forceReload();
    }

    public LiveData<Boolean> getLoadingStream() {
        return mTramLiveData.getLoadingData();
    }
}
