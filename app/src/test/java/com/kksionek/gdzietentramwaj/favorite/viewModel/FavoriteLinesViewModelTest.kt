package com.kksionek.gdzietentramwaj.favorite.viewModel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.favorite.repository.FavoriteVehiclesRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.map.view.UiState
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.subjects.ReplaySubject
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should equal`
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class FavoriteLinesViewModelTest {

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val lineId = "500"
    private val favorite = false
    private val selectedCity = Cities.WARSAW
    private val vehiclesSubject = ReplaySubject.create<List<FavoriteTram>>()
    private val favoriteVehiclesRepository: FavoriteVehiclesRepository = mock {
        on { getAllVehicles(selectedCity) } doReturn vehiclesSubject.toFlowable(BackpressureStrategy.LATEST)
        on { setVehicleFavorite(selectedCity, lineId, favorite) } doReturn Completable.complete()
    }
    private val crashReportingService: CrashReportingService = mock()
    private val mapSettingsProvider: MapSettingsProvider = mock {
        on { getCity() } doReturn selectedCity
    }

    private lateinit var tested: FavoriteLinesViewModel

    private val favoriteTramsObserver: Observer<UiState<List<FavoriteTram>>> = mock()

    private fun setupTested() {
        tested = FavoriteLinesViewModel(
            favoriteVehiclesRepository,
            crashReportingService,
            mapSettingsProvider
        )
        tested.favoriteTrams.observeForever(favoriteTramsObserver)
    }

    @Test
    fun `should get all vehicles on init`() {
        // given
        setupTested()

        // then
        verify(favoriteVehiclesRepository).getAllVehicles(selectedCity)
    }

    @Test
    fun `should emit in progress event on init`() {
        // given
        setupTested()

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(favoriteTramsObserver).onChanged(argCaptor.capture())
        argCaptor.firstValue `should be instance of` UiState.InProgress::class
    }

    @Test
    fun `should emit in progress event when reload requested`() {
        // given
        setupTested()

        // when
        tested.forceReloadFavorites()

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(favoriteTramsObserver, times(2)).onChanged(argCaptor.capture())
        argCaptor.secondValue `should be instance of` UiState.InProgress::class
    }

    @Test
    fun `should emit obtained vehicles when vehicles obtained`() {
        // given
        val vehicleList = listOf(FavoriteTram("1", false, selectedCity.id))
        setupTested()

        // when
        vehiclesSubject.onNext(vehicleList)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(favoriteTramsObserver, times(2)).onChanged(argCaptor.capture())
        with(argCaptor.lastValue) {
            this as UiState.Success
            this.data `should equal` vehicleList
        }
    }

    @Test
    fun `should emit error event when reload failed`() {
        // given
        val error: IOException = mock()
        setupTested()

        // when
        vehiclesSubject.onError(error)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(favoriteTramsObserver, times(2)).onChanged(argCaptor.capture())
        argCaptor.secondValue `should be instance of` UiState.Error::class
    }

    @Test
    fun `should report error event when reload failed`() {
        // given
        val error: IOException = mock()
        setupTested()

        // when
        vehiclesSubject.onError(error)

        // then
        verify(crashReportingService).reportCrash(
            error,
            "Failed getting all the favorites from the database"
        )
    }

    @Test
    fun `should skip results of old requests when reload requested two times given the first request didn't succeed until the second request`() {
        // given
        val vehicleList = listOf(FavoriteTram("1", false, selectedCity.id))
        val vehicleList2 = listOf(FavoriteTram("2", true, selectedCity.id))
        val abortedRequestSubject = ReplaySubject.create<List<FavoriteTram>>()
        whenever(favoriteVehiclesRepository.getAllVehicles(selectedCity)).thenReturn(
            abortedRequestSubject.toFlowable(BackpressureStrategy.LATEST),
            vehiclesSubject.toFlowable(BackpressureStrategy.LATEST)
        )
        setupTested()

        // when
        tested.forceReloadFavorites()
        tested.forceReloadFavorites()
        abortedRequestSubject.onNext(vehicleList)
        vehiclesSubject.onNext(vehicleList2)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(favoriteTramsObserver, times(4)).onChanged(argCaptor.capture())
        argCaptor.firstValue `should be instance of` UiState.InProgress::class
        argCaptor.secondValue `should be instance of` UiState.InProgress::class
        argCaptor.thirdValue `should be instance of` UiState.InProgress::class
        with(argCaptor.lastValue) {
            this as UiState.Success
            this.data `should equal` vehicleList2
        }
    }

    @Test
    fun `should persist the favorite tram when requested`() {
        // given
        setupTested()

        // when
        tested.setTramFavorite(lineId, favorite)

        // then
        verify(favoriteVehiclesRepository).setVehicleFavorite(selectedCity, lineId, favorite)
    }
}