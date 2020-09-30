package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.WroclawVehicleDataSource.Companion.dateFormat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.LocalDateTime

class WroclawVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val timestamp: String = dateFormat.format(LocalDateTime.now())
    private val wroclawVehicleInterface: WroclawVehicleInterface = mock {
        on { buses() } doReturn Single.just(
            WroclawVehicleResponse(
                WroclawVehicleList(
                    listOf(Record(52.0, 123, "500", "brigade", timestamp, 19.0))
                )
            )
        )
    }

    private val tested = WroclawVehicleDataSource(wroclawVehicleInterface)

    @Test
    fun `should return vehicle list when request succeeded`() {
        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 1
                        && it[0].id == "123"
                        && it[0].line == "500"
                        && it[0].brigade == "brigade"
                        && it[0].position == LatLng(19.0, 52.0)
                        && it[0].prevPosition == null
                        && !it[0].isTram
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(wroclawVehicleInterface.buses()).doReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should filter out outdated when request succeeded given some vehicles are outdated`() {
        // given
        val outdatedTimestamp = dateFormat.format(LocalDateTime.now().minusMinutes(3))
        whenever(wroclawVehicleInterface.buses()).thenReturn(
            Single.just(
                WroclawVehicleResponse(
                    WroclawVehicleList(
                        listOf(
                            Record(52.0, 123, "500", "brigade", timestamp, 19.0),
                            Record(52.1, 124, "501", "brigade", outdatedTimestamp, 19.1),
                            Record(52.2, 125, "502", "brigade", outdatedTimestamp, 19.2)
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
                        && it[0].id == "123"
                        && it[0].line == "500"
                        && it[0].brigade == "brigade"
                        && it[0].position == LatLng(19.0, 52.0)
                        && it[0].prevPosition == null
                        && !it[0].isTram
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should filter out vehicles with invalid line name when request succeeded given some vehicles have invalid line name`() {
        // given
        whenever(wroclawVehicleInterface.buses()).thenReturn(
            Single.just(
                WroclawVehicleResponse(
                    WroclawVehicleList(
                        listOf(
                            Record(52.0, 123, "500", "brigade", timestamp, 19.0),
                            Record(52.1, 124, "None", "brigade", timestamp, 19.1),
                            Record(52.2, 125, "None", "brigade", timestamp, 19.2)
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
                        && it[0].id == "123"
                        && it[0].line == "500"
                        && it[0].brigade == "brigade"
                        && it[0].position == LatLng(19.0, 52.0)
                        && it[0].prevPosition == null
                        && !it[0].isTram
            }
            .assertNoErrors()
            .assertComplete()
    }
}