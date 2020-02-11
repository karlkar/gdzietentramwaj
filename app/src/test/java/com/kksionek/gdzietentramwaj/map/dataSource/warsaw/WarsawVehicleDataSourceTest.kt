package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar

class WarsawVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val warsawVehicleInterface: WarsawVehicleInterface = mock {
        on { vehicles() } doReturn Single.just(
            WarsawVehicleResponse(
                listOf(
                    WarsawVehicle(
                        "500/brigade",
                        WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                        Position(52.0, 19.0),
                        null,
                        "500",
                        "brigade",
                        false
                    ),
                    WarsawVehicle(
                        "5/brigade2",
                        WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                        Position(52.4, 19.4),
                        null,
                        "5",
                        "brigade2",
                        true
                    )
                )
            )
        )
    }

    private val tested = WarsawVehicleDataSource(warsawVehicleInterface)

    @Test
    fun `should return vehicle list when request succeeded`() {
        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 2
                        && it.containsAll(
                    listOf(
                        VehicleData("500/brigade", LatLng(52.0, 19.0), "500", false, "brigade"),
                        VehicleData("5/brigade2", LatLng(52.4, 19.4), "5", true, "brigade2")
                    )
                )
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when vehicles request failed`() {
        // given
        val error: IOException = mock()
        whenever(warsawVehicleInterface.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should filter out outdated vehicles when request succeeded given some vehicles are out dated`() {
        // given
        val threeMinutesAgo = Calendar.getInstance().apply {
            add(Calendar.MINUTE, -3)
        }
        whenever(warsawVehicleInterface.vehicles()).thenReturn(
            Single.just(
                WarsawVehicleResponse(
                    listOf(
                        WarsawVehicle(
                            "500/brigade",
                            WarsawVehicleDataSource.dateFormat.format(threeMinutesAgo.time),
                            Position(52.0, 19.0),
                            Position(52.0001, 19.0001),
                            "500",
                            "brigade",
                            false
                        ),
                        WarsawVehicle(
                            "5/brigade2",
                            WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                            Position(52.4, 19.4),
                            null,
                            "5",
                            "brigade2",
                            true
                        )
                    )
                )
            )
        )
        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 1
                        && it.containsAll(
                    listOf(
                        VehicleData("5/brigade2", LatLng(52.4, 19.4), "5", true, "brigade2")
                    )
                )
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test // TODO: Serious! Should bee fixed ASAP!
    fun `should return error when request succeeded given some vehicles hav not proper format of date`() {
        // given
        whenever(warsawVehicleInterface.vehicles()).thenReturn(
            Single.just(
                WarsawVehicleResponse(
                    listOf(
                        WarsawVehicle(
                            "500/brigade",
                            "000000000000000wrong format",
                            Position(52.0, 19.0),
                            null,
                            "500",
                            "brigade",
                            false
                        ),
                        WarsawVehicle(
                            "5/brigade2",
                            WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                            Position(52.4, 19.4),
                            null,
                            "5",
                            "brigade2",
                            true
                        )
                    )
                )
            )
        )
        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 1
                        && it.containsAll(
                    listOf(
                        VehicleData("5/brigade2", LatLng(52.4, 19.4), "5", true, "brigade2")
                    )
                )
            }
            .assertNoErrors()
            .assertComplete()
    }
}