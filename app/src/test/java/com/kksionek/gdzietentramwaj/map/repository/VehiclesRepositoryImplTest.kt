package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class VehiclesRepositoryImplTest {

    private val testScheduler = TestScheduler()

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule(testScheduler)

    private val selectedCity = Cities.WARSAW
    private val vehicleData = VehicleData(
        "1",
        LatLng(52.0, 19.0),
        "500",
        false,
        "brigade"
    )
    private val vehicleList = listOf(vehicleData)
    private val vehicleDataSource: VehicleDataSource = mock {
        on { vehicles() } doReturn Single.just(vehicleList)
    }

    private val allVehicles = listOf(
        FavoriteTram("1", true, selectedCity.id),
        FavoriteTram("200", true, selectedCity.id)
    )

    private val tramDao: TramDao = mock {
        on { getAllVehicles(selectedCity.id) } doReturn Flowable.just(allVehicles)
    }
    private val vehicleDataSourceFactory: VehicleDataSourceFactory = mock {
        on { create(selectedCity) } doReturn vehicleDataSource
    }

    private val tested = VehiclesRepositoryImpl(tramDao, vehicleDataSourceFactory)

    @Test
    fun `should return vehicle list when requested`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Success && it.data === vehicleList }
            .assertValueCount(2)
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not break the stream when data source fails`() {
        // given
        val error: IOException = mock()
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Error && it.throwable === error }
            .assertValueCount(2)
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not complete the stream when requested`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress as first element when requested`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return difficulties from repository two times when subscription lasts long enough`() {
        // given
        val subscriptionTime = 70L
        val observer = tested.dataStream(selectedCity).test()

        // when
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Success).data == vehicleList
            }
            .assertValueAt(3) {
                (it as NetworkOperationResult.Success).data == vehicleList
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress when reloading vehicles`() {
        // given
        val subscriptionTime = 70L
        val observer = tested.dataStream(selectedCity).test()

        // when
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return vehicles from repository two times when reload was forced`() {
        // given
        val subscriptionTime = 10L
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // when
        tested.forceReload()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Success).data == vehicleList
            }
            .assertValueAt(3) {
                (it as NetworkOperationResult.Success).data == vehicleList
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress when reload was forced`() {
        // given
        val subscriptionTime = 10L
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // when
        tested.forceReload()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return error item when request failed`() {
        // given
        val error: IOException = mock()
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Error<List<VehicleData>>).throwable === error
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not complete the stream when request failed`() {
        // given
        val error: IOException = mock()
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not emit multiple values when requests force reload in period shorter than 1 minute`() {
        // given
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // when
        tested.forceReload()
        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS)
        tested.forceReload()
        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS)
        tested.forceReload()
        testScheduler.advanceTimeBy(8, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Success }
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertValueAt(3) { it is NetworkOperationResult.Success }
            .assertValueAt(4) { it is NetworkOperationResult.InProgress }
            .assertValueAt(5) { it is NetworkOperationResult.Success }
            .assertValueAt(6) { it is NetworkOperationResult.InProgress }
            .assertValueAt(7) { it is NetworkOperationResult.Success }
            .assertValueCount(8)
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should emit vehicles from other city when request fro other city comes after the first request`() {
        // given
        val otherCity = Cities.KRAKOW
        val otherCityDifficultiesDataSource: VehicleDataSource = mock {
            on { vehicles() } doReturn Single.just(emptyList())
        }
        whenever(vehicleDataSourceFactory.create(otherCity))
            .thenReturn(otherCityDifficultiesDataSource)
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.just(vehicleList))
        val observer = tested.dataStream(selectedCity).test()

        // when
        val otherObserver = tested.dataStream(otherCity).test()
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) {
                it is NetworkOperationResult.Success
                        && it.data == vehicleList
            }
            .assertNoErrors()
            .assertNotComplete()

        otherObserver
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) {
                it is NetworkOperationResult.Success
                        && it.data.isEmpty()
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not break the stream when error occurred in data source`() {
        // given
        val error: IOException = mock()
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(5, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Error && it.throwable === error }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should retry to obtain difficulties when error occurred in data source`() {
        // given
        val error: IOException = mock()
        whenever(vehicleDataSource.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(90, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Error && it.throwable === error }
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return all favorite vehicles when favorites requested`() {
        // when
        val observer = tested.getFavoriteVehicleLines(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValue { it == allVehicles.map { favTram -> favTram.lineId } }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should filter out not favorite vehicles when favorites requested`() {
        // given
        val allVehicles = listOf(
            FavoriteTram("1", true, selectedCity.id),
            FavoriteTram("200", false, selectedCity.id),
            FavoriteTram("500", true, selectedCity.id)
        )
        whenever(tramDao.getAllVehicles(selectedCity.id)).thenReturn(Flowable.just(allVehicles))

        // when
        val observer = tested.getFavoriteVehicleLines(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValue { result ->
                result == allVehicles.filter { it.isFavorite }.map { it.lineId }
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when favorites request failed`() {
        // given
        val error: IOException = mock()
        whenever(tramDao.getAllVehicles(selectedCity.id)).thenReturn(Flowable.error(error))

        // when
        val observer = tested.getFavoriteVehicleLines(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer.assertError { it === error }
    }
}