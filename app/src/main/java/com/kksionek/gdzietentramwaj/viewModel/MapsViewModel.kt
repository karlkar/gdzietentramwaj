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
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class MapsViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val locationRepository: LocationRepository,
    private val mapsViewSettingsRepository: MapsViewSettingsRepository
) : ViewModel() {

    private val favoriteView = MutableLiveData<Boolean>().apply {
        value = mapsViewSettingsRepository.isFavoriteTramViewEnabled()
    }

    private val _tramData = MutableLiveData<TramDataWrapper>()
    val tramData: LiveData<TramDataWrapper> = _tramData

    private val compositeDisposable = CompositeDisposable()

    private fun startFetchingTrams() {
        compositeDisposable.add(tramRepository.dataStream
            .subscribeOn(Schedulers.io())
            .subscribe { _tramData.postValue(it) })
    }

    private fun stopFetchingTrams() {
        compositeDisposable.clear()
    }

    fun isFavoriteViewEnabled(): LiveData<Boolean> = favoriteView

    fun getFavoriteTramsLiveData(): LiveData<List<String>> =
        tramRepository.favoriteTrams

    fun getLastKnownLocation(): Task<Location> =
        locationRepository.lastKnownLocation

    fun toggleFavorite() {
        val favoriteViewOn = !(favoriteView.value!!)
        mapsViewSettingsRepository.saveFavoriteTramViewState(favoriteViewOn)
        favoriteView.value = favoriteViewOn
    }

    fun forceReload() {
        tramRepository.forceReload()
    }

    fun onResume() {
        startFetchingTrams()
    }

    fun onPause() {
        stopFetchingTrams()
    }

    override fun onCleared() {
        stopFetchingTrams()
        super.onCleared()
    }
}
