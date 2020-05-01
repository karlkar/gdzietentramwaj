package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.Instant

class BielskoVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val vehicleLine = "1"
    private val vehicleId = 1
    private val vehicleBrigade = "brig1"
    private val vehicleLongitude = 1.0
    private val vehicleLatitude = 2.0
    private val vehicleTimestamp = Instant.now().toEpochMilli()

    private val bielskoVehicleInterface: BielskoVehicleInterface = mock {
        on { vehicles() } doReturn Single.just(
            BielskoVehicleList(
                listOf(
                    BielskoVehicle(
                        vehicleLine,
                        vehicleId,
                        vehicleBrigade,
                        vehicleLongitude,
                        vehicleLatitude,
                        vehicleTimestamp
                    )
                )
            )
        )
    }

    private val tested = BielskoVehicleDataSource(bielskoVehicleInterface)

    @Test
    fun `should return properly mapped vehicle when requested`() {
        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertValue {
            with(it[0]) {
                line == vehicleLine
                        && id == vehicleId.toString()
                        && brigade == vehicleBrigade
                        && position.latitude == vehicleLatitude
                        && position.longitude == vehicleLongitude
            }
        }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should filter out vehicles with old position when requested`() {
        // given
        whenever(bielskoVehicleInterface.vehicles()).thenReturn(
            Single.just(
                BielskoVehicleList(
                    listOf(
                        BielskoVehicle(
                            vehicleLine,
                            vehicleId,
                            vehicleBrigade,
                            vehicleLongitude,
                            vehicleLatitude,
                            1000L
                        )
                    )
                )
            )
        )
        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertValue { it.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }
}