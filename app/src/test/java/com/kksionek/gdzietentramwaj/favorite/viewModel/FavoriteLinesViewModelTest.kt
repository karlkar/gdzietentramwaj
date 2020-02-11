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
import com.nhaarman.mockitokotlin2.*
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

    private val tested = FavoriteLinesViewModel(
        favoriteVehiclesRepository,
        crashReportingService,
        mapSettingsProvider
    )

    @Test
    fun `should emit in progress event when reload requested`() {
        // given
        val observer: Observer<UiState<List<FavoriteTram>>> = mock()
        tested.favoriteTrams.observeForever(observer)

        // when
        tested.forceReloadFavorites()

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(observer, times(1)).onChanged(argCaptor.capture())
        argCaptor.firstValue `should be instance of` UiState.InProgress::class
    }

    @Test
    fun `should emit obtained vehicles when vehicles obtained`() {
        // given
        val observer: Observer<UiState<List<FavoriteTram>>> = mock()
        val vehicleList = listOf(FavoriteTram("1", false, selectedCity.id))
        tested.favoriteTrams.observeForever(observer)

        // when
        tested.forceReloadFavorites()
        vehiclesSubject.onNext(vehicleList)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(observer, times(2)).onChanged(argCaptor.capture())
        with(argCaptor.lastValue) {
            this as UiState.Success
            this.data `should equal` vehicleList
        }
    }

    @Test
    fun `should emit error event when reload failed`() {
        // given
        val error: IOException = mock()
        val observer: Observer<UiState<List<FavoriteTram>>> = mock()
        tested.favoriteTrams.observeForever(observer)

        // when
        tested.forceReloadFavorites()
        vehiclesSubject.onError(error)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(observer, times(2)).onChanged(argCaptor.capture())
        argCaptor.secondValue `should be instance of` UiState.Error::class
    }

    @Test
    fun `should report error event when reload failed`() {
        // given
        val error: IOException = mock()
        val observer: Observer<UiState<List<FavoriteTram>>> = mock()
        tested.favoriteTrams.observeForever(observer)

        // when
        tested.forceReloadFavorites()
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
        val observer: Observer<UiState<List<FavoriteTram>>> = mock()
        val vehicleList = listOf(FavoriteTram("1", false, selectedCity.id))
        val vehicleList2 = listOf(FavoriteTram("2", true, selectedCity.id))
        val abortedRequestSubject = ReplaySubject.create<List<FavoriteTram>>()
        whenever(favoriteVehiclesRepository.getAllVehicles(selectedCity)).thenReturn(
            abortedRequestSubject.toFlowable(BackpressureStrategy.LATEST),
            vehiclesSubject.toFlowable(BackpressureStrategy.LATEST)
        )
        tested.favoriteTrams.observeForever(observer)

        // when
        tested.forceReloadFavorites()
        tested.forceReloadFavorites()
        abortedRequestSubject.onNext(vehicleList)
        vehiclesSubject.onNext(vehicleList2)

        // then
        val argCaptor = argumentCaptor<UiState<List<FavoriteTram>>>()
        verify(observer, times(3)).onChanged(argCaptor.capture())
        argCaptor.firstValue `should be instance of` UiState.InProgress::class
        argCaptor.secondValue `should be instance of` UiState.InProgress::class
        with(argCaptor.thirdValue) {
            this as UiState.Success
            this.data `should equal` vehicleList2
        }
    }

    @Test
    fun `should persist the favorite tram when requested`() {
        // when
        tested.setTramFavorite(lineId, favorite)

        // then
        verify(favoriteVehiclesRepository).setVehicleFavorite(selectedCity, lineId, favorite)
    }
}