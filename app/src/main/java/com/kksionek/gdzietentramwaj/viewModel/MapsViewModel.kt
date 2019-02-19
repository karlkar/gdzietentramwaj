package com.kksionek.gdzietentramwaj.viewModel

import android.app.Application
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import android.preference.PreferenceManager
import com.google.android.gms.tasks.Task
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.repository.LocationRepository
import com.kksionek.gdzietentramwaj.repository.TramRepository
import javax.inject.Inject

class MapsViewModel @Inject constructor(
    private val context: Application,
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val mFavoriteView = MutableLiveData<Boolean>()

    init {
        val favoriteTramView = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getBoolean("FAVORITE_TRAM_VIEW", false)  // TODO: Inject sharedPrefs
        mFavoriteView.value = favoriteTramView
    }

    val tramData: LiveData<TramDataWrapper> by lazy {
        tramRepository.dataStream
    }

    fun isFavoriteViewEnabled(): LiveData<Boolean> = mFavoriteView

    fun getFavoriteTramsLiveData(): LiveData<List<String>> =
        tramRepository.favoriteTrams

    fun getLastKnownLocation(): Task<Location> =
        locationRepository.lastKnownLocation

    fun toggleFavorite() {
        val favoriteViewOn = !(mFavoriteView.value!!)
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putBoolean("FAVORITE_TRAM_VIEW", favoriteViewOn)
            .apply()
        mFavoriteView.value = favoriteViewOn
    }

    fun forceReload() {
        tramRepository.forceReload()
    }
}
