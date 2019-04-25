package com.kksionek.gdzietentramwaj.onboarding.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.maps.android.SphericalUtil
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import com.kksionek.gdzietentramwaj.toLatLng
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class OnBoardingViewModel @Inject constructor(
    private val versionRepository: VersionRepository,
    private val mapSettingsManager: MapSettingsManager,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val skipOnBoarding: Boolean
        get() = lastVersion >= FIRST_APP_VERSION_MULTICITY

    private val _nearestCity = MutableLiveData<Cities>().apply { value = null }
    val nearestCity: LiveData<Cities> = _nearestCity

    var city: Cities
        get() = mapSettingsManager.getCity()
        set(value) = mapSettingsManager.setCity(value)

    private var lastVersion: Int
        get() = versionRepository.getPreviouslyLaunchedVersion()
        set(value) = versionRepository.saveLastLaunchedVersion(value)

    fun updateLastVersion() {
        lastVersion = BuildConfig.VERSION_CODE
    }

    private val compositeDisposable = CompositeDisposable()

    fun forceReloadLocation() {
        compositeDisposable.add(locationRepository.lastKnownLocation.subscribe { location: Location ->
            val nearestCity = findNearestCity(location)
            _nearestCity.postValue(nearestCity)
        })
    }

    private fun findNearestCity(location: Location): Cities {
        val userPosition = location.toLatLng()
        return Cities.values()
            .minBy { SphericalUtil.computeDistanceBetween(it.latLng, userPosition) }
            ?: Cities.WARSAW
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    companion object {
        const val FIRST_APP_VERSION_MULTICITY = 41
    }
}