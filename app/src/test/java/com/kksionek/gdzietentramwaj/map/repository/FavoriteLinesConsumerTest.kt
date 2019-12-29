package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

class FavoriteLinesConsumerTest {

    private val vehicleBus = VehicleData(
        "1",
        LatLng(52.0, 19.0),
        "500",
        false,
        "brigade1"
    )
    private val vehicleTram = VehicleData(
        "2",
        LatLng(52.4, 19.4),
        "25",
        true,
        "brigade2"
    )
    private val tramDao: TramDao = mock()
    private val selectedCity = Cities.WARSAW

    private val tested = FavoriteLinesConsumer(tramDao, selectedCity)

    @Test
    fun `should save all the vehicles in favorites database when requested`() {
        // given
        val vehicles = listOf(
            vehicleBus,
            vehicleTram
        )

        // when
        tested.accept(vehicles)

        // then
        verify(tramDao, times(1)).save(argThat<FavoriteTram> {
            this.lineId == "500"
                    && this.cityId == selectedCity.id
                    && !this.isFavorite
        })
        verify(tramDao, times(1)).save(argThat<FavoriteTram> {
            this.lineId == "25"
                    && this.cityId == selectedCity.id
                    && !this.isFavorite
        })
    }

    @Test
    fun `should ignore duplicate entries when requested given duplicates exist in the list`() {
        // given
        val vehicles = listOf(
            vehicleTram,
            vehicleTram,
            vehicleTram,
            vehicleTram,
            vehicleTram
        )

        // when
        tested.accept(vehicles)

        // then
        verify(tramDao, times(1)).save(argThat<FavoriteTram> {
            this.lineId == "25"
                    && this.cityId == selectedCity.id
                    && !this.isFavorite
        })
    }

    @Test
    fun `should not save anything when requested given the list is empty`() {
        // given
        val vehicles = emptyList<VehicleData>()

        // when
        tested.accept(vehicles)

        // then
        verify(tramDao, never()).save(any<FavoriteTram>())
    }
}