package com.kksionek.gdzietentramwaj.main.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.amshove.kluent.`should be less or equal to`
import org.amshove.kluent.`should be`
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class MainViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val lastLocation = LatLng(1.0, 1.0)
    private val selectedCity = Cities.WARSAW
    private val locationRepository: LocationRepository = mock {
        on { lastKnownLocation } doReturn Single.just(lastLocation)
    }
    private val mapSettingsProvider: MapSettingsProvider = mock {
        on { getCity() } doReturn selectedCity
    }
    private val appUpdateRepository: AppUpdateRepository = mock {
        on { isUpdateAvailable() } doReturn Single.just(false)
    }
    private val googleApiAvailabilityChecker: GoogleApiAvailabilityChecker = mock()

    private lateinit var tested: MainViewModel

    private fun initialize() {
        tested = MainViewModel(
            locationRepository,
            mapSettingsProvider,
            appUpdateRepository,
            googleApiAvailabilityChecker
        )
    }

    @Test
    fun `should obtain last known location when initialized`() {
        // when
        initialize()

        // then
        verify(locationRepository).lastKnownLocation
    }

    @Test
    fun `should check for new version availability when initialized`() {
        // when
        initialize()

        // then
        verify(appUpdateRepository).isUpdateAvailable()
    }

    @Test
    fun `should set last known location when request for last location successful`() {
        // given
        val mockObserver: Observer<LatLng> = mock()

        // when
        initialize()
        tested.lastLocation.observeForever(mockObserver)

        // then
        tested.lastLocation.value `should be` lastLocation
    }

    @Test
    fun `should set selected city's location when request for last location failed`() {
        // given
        val mockObserver: Observer<LatLng> = mock()
        val error = IOException()
        whenever(locationRepository.lastKnownLocation).thenReturn(Single.error(error))

        // when
        initialize()
        tested.lastLocation.observeForever(mockObserver)

        // then
        (tested.lastLocation.value!!.latitude - selectedCity.latLng.latitude) `should be less or equal to` 0.00000001
        (tested.lastLocation.value!!.longitude - selectedCity.latLng.longitude) `should be less or equal to` 0.00000001
    }
}