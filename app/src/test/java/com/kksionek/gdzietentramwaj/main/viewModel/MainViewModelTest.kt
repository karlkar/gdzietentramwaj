package com.kksionek.gdzietentramwaj.main.viewModel

import android.app.Activity
import android.content.DialogInterface
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.google.android.gms.maps.model.LatLng
import com.google.android.play.core.install.model.AppUpdateType
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.amshove.kluent.`should be less or equal to`
import org.amshove.kluent.`should be`
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.math.abs

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
        on { isUpdateAvailable() } doReturn Single.just(true)
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
        initialize()

        // when
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
        initialize()

        // when
        tested.lastLocation.observeForever(mockObserver)

        // then
        abs(tested.lastLocation.value!!.latitude - selectedCity.latLng.latitude) `should be less or equal to` 0.00000001
        abs(tested.lastLocation.value!!.longitude - selectedCity.latLng.longitude) `should be less or equal to` 0.00000001
    }

    @Test
    fun `should set new version available when request for new version successful given new version is available`() {
        // given
        val mockObserver: Observer<Boolean> = mock()
        initialize()

        // when
        tested.appUpdateAvailable.observeForever(mockObserver)

        // then
        tested.appUpdateAvailable.value `should be` true
    }

    @Test
    fun `should set new version not available when request for new version failed`() {
        // given
        val error: IOException = mock()
        val mockObserver: Observer<Boolean> = mock()
        whenever(appUpdateRepository.isUpdateAvailable()).thenReturn(Single.error(error))
        initialize()

        // when
        tested.appUpdateAvailable.observeForever(mockObserver)

        // then
        tested.appUpdateAvailable.value `should be` false
    }

    @Test
    fun `should trigger location permission request when requested`() {
        // given
        whenever(locationRepository.isLocationPermissionGranted()).thenReturn(false)
        initialize()

        // when
        tested.requestLocationPermission()

        // then
        tested.locationPermissionRequestLiveData.value `should be` true
    }

    @Test
    fun `should not trigger location permission request when requested given permission is already granted`() {
        // given
        whenever(locationRepository.isLocationPermissionGranted()).thenReturn(true)
        initialize()

        // when
        tested.requestLocationPermission()

        // then
        tested.locationPermissionRequestLiveData.value `should be` null
    }

    @Test
    fun `should emit location permission granted when permission result was obtained`() {
        // given
        val result = false
        initialize()

        // when
        tested.onRequestPermissionsResult(result)

        // then
        tested.locationPermission.value `should be` result
    }

    @Test
    fun `should start update flow when requested`() {
        // given
        val activity: Activity = mock()
        initialize()

        // when
        tested.startUpdateFlowForResult(activity)

        // then
        verify(appUpdateRepository).startUpdateFlowForResult(
            AppUpdateType.IMMEDIATE,
            activity,
            APP_UPDATE_AVAILABILITY_REQUEST_CODE
        )
    }

    @Test
    fun `should start update flow when activity is resumed given update was started`() {
        // given
        val activity: Activity = mock()
        whenever(appUpdateRepository.isUpdateInProgress()).thenReturn(Single.just(true))
        initialize()

        // when
        tested.onResume(activity)

        // then
        verify(appUpdateRepository).startUpdateFlowForResult(
            AppUpdateType.IMMEDIATE,
            activity,
            APP_UPDATE_AVAILABILITY_REQUEST_CODE
        )
    }

    @Test
    fun `should not start update flow when activity is resumed given update was not started`() {
        // given
        val activity: Activity = mock()
        whenever(appUpdateRepository.isUpdateInProgress()).thenReturn(Single.just(false))
        initialize()

        // when
        tested.onResume(activity)

        // then
        verify(appUpdateRepository, never()).startUpdateFlowForResult(any(), any(), any())
    }

    @Test
    fun `should not start update flow when activity is resumed given couldn't check update progress`() {
        // given
        val activity: Activity = mock()
        whenever(appUpdateRepository.isUpdateInProgress()).thenReturn(Single.just(false))
        initialize()

        // when
        tested.onResume(activity)

        // then
        verify(appUpdateRepository, never()).startUpdateFlowForResult(any(), any(), any())
    }

    @Test
    fun `should show google api update needed dialog when requested`() {
        // given
        val activity: Activity = mock()
        val callback: (DialogInterface) -> Unit = mock()
        initialize()

        // when
        tested.showGoogleApiUpdateNeededDialog(activity, callback)

        // then
        verify(googleApiAvailabilityChecker).showGoogleApiUpdateNeededDialog(
            activity,
            GOOGLE_API_AVAILABILITY_REQUEST_CODE,
            callback
        )
    }
}