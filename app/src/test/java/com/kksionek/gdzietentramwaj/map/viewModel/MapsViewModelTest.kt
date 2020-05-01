package com.kksionek.gdzietentramwaj.map.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.model.MapTypes
import com.kksionek.gdzietentramwaj.map.repository.DifficultiesRepository
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.VehiclesRepository
import com.kksionek.gdzietentramwaj.map.view.MapControls
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test

class MapsViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val selectedCity = Cities.WARSAW

    private val vehiclesRepository: VehiclesRepository = mock()
    private val locationRepository: LocationRepository = mock {
        on { lastKnownLocation } doReturn Single.just(Cities.WARSAW.latLng)
    }
    private val mapsViewSettingsRepository: MapsViewSettingsRepository = mock {
        on { isFavoriteTramViewEnabled() } doReturn false
    }
    private val difficultiesRepository: DifficultiesRepository = mock()
    private val crashReportingService: CrashReportingService = mock()
    private val iconSettingsProvider: IconSettingsProvider = mock()
    private val mapSettingsManager: MapSettingsManager = mock {
        on { getCity() } doReturn selectedCity
    }

    private lateinit var tested: MapsViewModel

    private fun initialize() {
        tested = MapsViewModel(
            vehiclesRepository,
            locationRepository,
            mapsViewSettingsRepository,
            difficultiesRepository,
            crashReportingService,
            iconSettingsProvider,
            mapSettingsManager
        )
    }

    @Test
    fun `should get favorite view state from repository when initialized`() {
        // given
        initialize()

        val observer: Observer<Boolean> = mock()
        tested.favoriteView.observeForever(observer)

        // when
        // initialized

        // then
        val argCaptor = argumentCaptor<Boolean>()
        verify(observer).onChanged(argCaptor.capture())
        argCaptor.firstValue `should be` false
    }

    @Test
    fun `should return start location at selected city center when initialized given user has not overridden the start position and start position override is disabled`() {
        // given
        whenever(mapSettingsManager.getStartLocationPosition()).thenReturn(null)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(false)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialPosition `should be` selectedCity.latLng
    }

    @Test
    fun `should return start location at selected city center when initialized given user has not overridden the start position but start position override is enabled`() {
        // given
        whenever(mapSettingsManager.getStartLocationPosition()).thenReturn(null)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(true)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialPosition `should be` selectedCity.latLng
    }

    @Test
    fun `should return overridden location when initialized given user has overridden the start position`() {
        // given
        whenever(mapSettingsManager.getStartLocationPosition()).thenReturn(Cities.BIELSKO.latLng)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(true)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialPosition `should be` Cities.BIELSKO.latLng
    }

    @Test
    fun `should return default zoom level when initialized given user has not overridden the default zoom level and start position override is disabled`() {
        // given
        whenever(mapSettingsManager.getDefaultZoom()).thenReturn(15.0f)
        whenever(mapSettingsManager.getStartLocationZoom()).thenReturn(10.0f)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(false)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialZoom `should equal` 15.0f
    }

    @Test
    fun `should return default zoom level when initialized given user has not overridden the default zoom level and start position override is enabled`() {
        // given
        whenever(mapSettingsManager.getDefaultZoom()).thenReturn(15.0f)
        whenever(mapSettingsManager.getStartLocationZoom()).thenReturn(null)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(true)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialZoom `should equal` 15.0f
    }

    @Test
    fun `should return overridden zoom level when initialized given user has overridden the default zoom level and start position override is enabled`() {
        // given
        whenever(mapSettingsManager.getDefaultZoom()).thenReturn(15.0f)
        whenever(mapSettingsManager.getStartLocationZoom()).thenReturn(10.0f)
        whenever(mapSettingsManager.isStartLocationEnabled()).thenReturn(true)
        initialize()

        // when
        // initialized

        // then
        tested.mapInitialZoom `should equal` 10.0f
    }

    @Test
    fun `should save the changed map type when user has changed the map type`() {
        // given
        val mapType = MapTypes.NORMAL
        whenever(mapSettingsManager.getMapType()).thenReturn(mapType)
        initialize()
        val observer: Observer<MapControls> = mock()
        tested.mapControls.observeForever(observer)

        // when
        tested.onSwitchMapTypeButtonClicked()

        // then
        verify(mapSettingsManager).setMapType(mapType.next())
    }

    @Test
    fun `should change the map type when user has changed the map type`() {
        // given
        val mapType = MapTypes.NORMAL
        whenever(mapSettingsManager.getMapType()).thenReturn(mapType)
        initialize()
        val observer: Observer<MapControls> = mock()
        tested.mapControls.observeForever(observer)

        // when
        tested.onSwitchMapTypeButtonClicked()

        // then
        val argCaptor = argumentCaptor<MapControls>()
        verify(observer, times(2)).onChanged(argCaptor.capture())
        argCaptor.secondValue `should equal` MapControls.ChangeType(mapType.next())
    }

    @Test
    fun `should change the map type multiple times when user has changed the map type multiple times`() {
        // given
        val mapType = MapTypes.NORMAL
        whenever(mapSettingsManager.getMapType()).thenReturn(
            mapType,
            mapType.next(),
            mapType.next().next()
        )
        initialize()
        val observer: Observer<MapControls> = mock()
        tested.mapControls.observeForever(observer)

        // when
        tested.onSwitchMapTypeButtonClicked()
        tested.onSwitchMapTypeButtonClicked()
        tested.onSwitchMapTypeButtonClicked()

        // then
        val argCaptor = argumentCaptor<MapControls>()
        verify(observer, times(4)).onChanged(argCaptor.capture())
        argCaptor.secondValue `should equal` MapControls.ChangeType(mapType.next())
        argCaptor.thirdValue `should equal` MapControls.ChangeType(mapType.next().next())
        argCaptor.allValues[3] `should equal` MapControls.ChangeType(mapType.next().next().next())
    }

    @Test
    fun `should change the map position to selected city center when initialized`() {
        // given
        initialize()
        val observer: Observer<MapControls> = mock()
        tested.mapControls.observeForever(observer)

        // when
        // initialized

        // then
        val argCaptor = argumentCaptor<MapControls>()
        verify(observer).onChanged(argCaptor.capture())
        argCaptor.firstValue `should equal` MapControls.MoveTo(selectedCity.latLng, false)
    }

    @Test
    fun `should get map type when requested`() {
        // given
        val setMapType = MapTypes.NORMAL
        whenever(mapSettingsManager.getMapType()).thenReturn(setMapType)
        initialize()

        // when
        val mapType = tested.getMapType()

        // then
        mapType `should be` setMapType
    }

    @Test
    fun `should get old icon set settings when requested`() {
        // given
        whenever(iconSettingsProvider.isOldIconSetEnabled()).thenReturn(false)
        initialize()

        // when
        val oldIconSet = tested.isOldIconSetEnabled

        // then
        oldIconSet `should be` false
    }

    @Test
    fun `should get traffic view settings when requested`() {
        // given
        whenever(mapSettingsManager.isTrafficShowingEnabled()).thenReturn(false)
        initialize()

        // when
        val trafficShowSet = tested.isTrafficShowingEnabled

        // then
        trafficShowSet `should be` false
    }
}