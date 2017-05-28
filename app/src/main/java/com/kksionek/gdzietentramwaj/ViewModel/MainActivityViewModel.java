package com.kksionek.gdzietentramwaj.ViewModel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.location.Location;

import com.kksionek.gdzietentramwaj.Repository.LocationRepository;
import com.kksionek.gdzietentramwaj.Repository.TramRepository;
import com.kksionek.gdzietentramwaj.TramApplication;
import com.kksionek.gdzietentramwaj.DataSource.TramData;

import java.util.List;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> mFavoriteView = new MutableLiveData<>();
    private LiveData<Boolean> mLoadingLiveData = null;
    private LiveData<List<TramData>> mTramLiveData = null;
    private TramRepository mTramRepository;
    private LocationRepository mLocationRepository;

    public MainActivityViewModel() {
        super();
        mFavoriteView.setValue(false);
        mTramRepository = TramApplication.getAppComponent().getTramRepository();
        mLocationRepository = TramApplication.getAppComponent().getLocationRepository();
    }

    public LiveData<List<TramData>> getTramData() {
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
        mFavoriteView.setValue(!mFavoriteView.getValue());
    }

    public LiveData<Boolean> getLoadingLiveData() {
        return mLoadingLiveData;
    }

    public void forceReload() {
        mTramRepository.forceReload();
    }

    public boolean isTramFavorite(String tramLine) {
        return false;
    }

    public LiveData<Location> getLocationLiveData() {
        return mLocationRepository.getLocationLiveData();
    }
}
