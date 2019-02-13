package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.repository.LocationRepository
import com.kksionek.gdzietentramwaj.repository.TramRepository

class MainActivityViewModel : ViewModel() {

    private val mFavoriteView = MutableLiveData<Boolean>()
    private val mTramRepository: TramRepository
    private val mLocationRepository: LocationRepository

    init {
        val favoriteTramView = PreferenceManager
            .getDefaultSharedPreferences(TramApplication.getAppComponent().appContext)
            .getBoolean("FAVORITE_TRAM_VIEW", false)  // TODO: Inject context, sharedPrefs
        mFavoriteView.value = favoriteTramView
        mTramRepository = TramApplication.getAppComponent().tramRepository
        mLocationRepository = TramApplication.getAppComponent().locationRepository
    }

    val tramData: LiveData<TramDataWrapper> by lazy {
        mTramRepository.dataStream
    }

    fun isFavoriteViewEnabled(): LiveData<Boolean> = mFavoriteView

    fun getFavoriteTramsLiveData(): LiveData<List<String>> =
        mTramRepository.favoriteTrams

    fun getLastKnownLocation(): Task<Location> =
        mLocationRepository.lastKnownLocation

    fun toggleFavorite() {
        val favoriteViewOn = !(mFavoriteView.value!!)
        PreferenceManager
            .getDefaultSharedPreferences(TramApplication.getAppComponent().appContext)
            .edit()
            .putBoolean("FAVORITE_TRAM_VIEW", favoriteViewOn)
            .apply()
        mFavoriteView.value = favoriteViewOn
    }

    fun forceReload() {
        mTramRepository.forceReload()
    }
}