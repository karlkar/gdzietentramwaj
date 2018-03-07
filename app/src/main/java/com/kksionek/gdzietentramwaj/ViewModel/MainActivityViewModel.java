package com.kksionek.gdzietentramwaj.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.preference.PreferenceManager;

import com.kksionek.gdzietentramwaj.DataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.Repository.LocationRepository;
import com.kksionek.gdzietentramwaj.Repository.TramRepository;
import com.kksionek.gdzietentramwaj.TramApplication;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mFavoriteView = new MutableLiveData<>();
    private LiveData<Boolean> mLoadingLiveData = null;
    private LiveData<TramDataWrapper> mTramLiveData = null;
    private TramRepository mTramRepository;
    private LocationRepository mLocationRepository;

    public MainActivityViewModel() {
        super();
        boolean favoriteTramView = PreferenceManager
                .getDefaultSharedPreferences(TramApplication.getAppComponent().getAppContext())
                .getBoolean("FAVORITE_TRAM_VIEW", false);
        mFavoriteView.setValue(favoriteTramView);
        mTramRepository = TramApplication.getAppComponent().getTramRepository();
        mLocationRepository = TramApplication.getAppComponent().getLocationRepository();
    }

    public LiveData<TramDataWrapper> getTramData() {
        if (mTramLiveData == null) {
            mLoadingLiveData = mTramRepository.getLoadingStream();
            mTramLiveData = mTramRepository.getDataStream();
        }
        return mTramLiveData;
    }

    public LiveData<Boolean> isFavoriteView() {
        return mFavoriteView;
    }

    public void toggleFavorite() {
        boolean favoriteViewOn = !mFavoriteView.getValue();
        PreferenceManager
                .getDefaultSharedPreferences(TramApplication.getAppComponent().getAppContext())
                .edit()
                .putBoolean("FAVORITE_TRAM_VIEW", favoriteViewOn)
                .apply();
        mFavoriteView.setValue(favoriteViewOn);
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return mLoadingLiveData;
    }

    public void forceReload() {
        mTramRepository.forceReload();
    }

    public LiveData<Location> getLocationLiveData() {
        return mLocationRepository.getLocationLiveData();
    }

    public LiveData<List<String>> getFavoriteTramsLiveData() {
        return mTramRepository.getFavoriteTrams();
    }
}
