package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.reactivex.functions.Consumer;

public class TramRepository {
    private static final String TAG = "TramRepository";

    private final TramLiveData mTramLiveData;
    private final TramDao mTramDao;
    private final FavoriteLinesConsumer mListConsumer;

    @Inject
    public TramRepository(TramDao tramDao, TramInterface tramInterface) {
        mTramDao = tramDao;
        mListConsumer = new FavoriteLinesConsumer(mTramDao);
        mTramLiveData = new TramLiveData(tramInterface, mListConsumer);
    }

    public LiveData<TramDataWrapper> getDataStream() {
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
