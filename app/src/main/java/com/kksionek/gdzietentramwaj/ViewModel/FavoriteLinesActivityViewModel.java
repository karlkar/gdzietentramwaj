package com.kksionek.gdzietentramwaj.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.kksionek.gdzietentramwaj.Repository.TramRepository;
import com.kksionek.gdzietentramwaj.TramApplication;
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram;

import java.util.List;

public class FavoriteLinesActivityViewModel extends ViewModel {

    private final TramRepository mTramRepository;

    public FavoriteLinesActivityViewModel() {
        mTramRepository = TramApplication.getAppComponent().getTramRepository();
    }

    public LiveData<List<FavoriteTram>> getFavoriteTrams() {
        return mTramRepository.getAllFavTrams();
    }

    public void setTramFavorite(String lineId, boolean favorite) {
        mTramRepository.setTramFavorite(lineId, favorite);
    }
}
