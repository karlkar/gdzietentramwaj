package com.kksionek.gdzietentramwaj.Repository;

import android.arch.lifecycle.LiveData;

import com.kksionek.gdzietentramwaj.DataSource.Room.FavoriteTram;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramData;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

public class TramRepository {
    private static final String TAG = "TramRepository";

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
    private final TramLiveData mTramLiveData;
    private final TramDao mTramDao;

    @Inject
    public TramRepository(TramDao tramDao, TramInterface tramInterface) {
        mTramDao = tramDao;
        mTramLiveData = new TramLiveData(tramInterface, tramData -> {
            HashSet<String> done = new HashSet<>();
            long currentTime = System.currentTimeMillis();
            for (TramData tram : tramData) {
                try {
                    if (currentTime - mDateFormat.parse(tram.getTime()).getTime() > 60000) {
                        tram.setTooOld();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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
