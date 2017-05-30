package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.inject.Inject;

public class TramRepository {
    private static final String TAG = "TramRepository";

    private final TramLiveData mTramLiveData;
    private final TramDao mTramDao;

    @Inject
    public TramRepository(TramDao tramDao, TramInterface tramInterface) {
        mTramDao = tramDao;
        mTramLiveData = new TramLiveData(tramInterface, tramData -> {
            HashSet<String> done = new HashSet<>();
            for (TramData tram : tramData) {
                if (!done.contains(tram.getFirstLine())) {
                    tramDao.save(new FavoriteTram(tram.getFirstLine(), false));
                    done.add(tram.getFirstLine());
                }
            }
        });
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

    public LiveData<List<FavoriteTram>> getAllFavTrams() {
        return mTramDao.getAllFavTrams();
    }

    public LiveData<List<String>> getFavoriteTrams() {
        return mTramDao.getFavoriteTrams();
    }

    public void setTramFavorite(String lineId, boolean favorite) {
        mTramDao.setFavorite(lineId, favorite);
    }
}
