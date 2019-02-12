package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;

import com.kksionek.gdzietentramwaj.dataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.dataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.dataSource.TramInterface;

import java.util.List;

import javax.inject.Inject;

public class TramRepository {
    private static final String TAG = "TramRepository";

    private final TramLiveData mTramLiveData;
    private final TramDao mTramDao;
    private final FavoriteLinesConsumer mFavoriteRepositoryAdder;

    @Inject
    public TramRepository(TramDao tramDao, TramInterface tramInterface) {
        mTramDao = tramDao;
        mFavoriteRepositoryAdder = new FavoriteLinesConsumer(mTramDao);
        mTramLiveData = new TramLiveData(tramInterface, mFavoriteRepositoryAdder);
    }

    public LiveData<TramDataWrapper> getDataStream() {
        return mTramLiveData;
    }

    public void forceReload() {
        mTramLiveData.forceReload();
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
