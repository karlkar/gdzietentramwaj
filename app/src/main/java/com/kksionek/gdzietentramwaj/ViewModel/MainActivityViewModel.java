package com.kksionek.gdzietentramwaj.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.tasks.Task;
import com.kksionek.gdzietentramwaj.TramApplication;
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper;
import com.kksionek.gdzietentramwaj.repository.LocationRepository;
import com.kksionek.gdzietentramwaj.repository.TramRepository;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mFavoriteView = new MutableLiveData<>();
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

    public void forceReload() {
        mTramRepository.forceReload();
    }

    public LiveData<List<String>> getFavoriteTramsLiveData() {
        return mTramRepository.getFavoriteTrams();
    }

    public Task<Location> getLastKnownLocation() {
        return mLocationRepository.getLastKnownLocation();
    }
}
