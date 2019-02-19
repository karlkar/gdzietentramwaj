package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.location.Location
import com.google.android.gms.tasks.Task
import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.repository.LocationRepository
import com.kksionek.gdzietentramwaj.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.repository.TramRepository
import javax.inject.Inject

class MapsViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository
) : ViewModel() {

    private val mFavoriteView = MutableLiveData<Boolean>()

    init {
        mFavoriteView.value = mapsViewSettingsRepository.isFavoriteTramViewEnabled()
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
        mapsViewSettingsRepository.saveFavoriteTramViewState(favoriteViewOn)
        mFavoriteView.value = favoriteViewOn
    }

    fun forceReload() {
        tramRepository.forceReload()
    }
}
