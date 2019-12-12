package com.kksionek.gdzietentramwaj.favorite.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.ReplaySubject
import org.junit.Test
import java.io.IOException

class FavoriteVehiclesRepositoryTest {

    private val city = Cities.WARSAW
    private val vehicleResponseSubject = ReplaySubject.create<List<FavoriteTram>>()
    private val tramDao: TramDao = mock {
        on { getAllVehicles(city.id) } doReturn vehicleResponseSubject
                .toFlowable(BackpressureStrategy.LATEST)
    }

    private val tested = FavoriteVehiclesRepository(tramDao)

    @Test
    fun `should return vehicles from the requested city when request is successful`() {
        // given
        val vehicles = listOf(
                FavoriteTram("12", false, city.id),
                FavoriteTram("500", true, city.id)
        )
        vehicleResponseSubject.onNext(vehicles)

        // when
        val observer = tested.getAllVehicles(city).test()

        // then
        observer.assertNoErrors()
                .assertValue(vehicles)
                .assertNotComplete()
    }

    @Test
    fun `should skip duplicate emissions until list changes when getting the vehicles`() {
        // given
        val vehicles = listOf(
                FavoriteTram("12", false, city.id),
                FavoriteTram("500", true, city.id)
        )
        val vehicles2 = listOf(
                FavoriteTram("12", false, city.id),
                FavoriteTram("500", true, city.id),
                FavoriteTram("501", false, city.id)
        )
        vehicleResponseSubject.onNext(vehicles)
        vehicleResponseSubject.onNext(vehicles)
        vehicleResponseSubject.onNext(vehicles2)

        // when
        val observer = tested.getAllVehicles(city).test()

        // then
        observer.assertNoErrors()
                .assertValueCount(2)
                .assertValues(vehicles, vehicles2)
                .assertNotComplete()
    }

    @Test
    fun `should return exception when getting the vehicles fails`() {
        // given
        val exception: IOException = mock()
        vehicleResponseSubject.onError(exception)

        // when
        val observer = tested.getAllVehicles(city).test()

        // then
        observer.assertError(exception)
    }
}