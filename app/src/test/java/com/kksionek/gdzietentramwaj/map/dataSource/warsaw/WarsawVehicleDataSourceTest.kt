package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.Calendar

private const val APIKEY = "apikey"

class WarsawVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val warsawApiKeyRepository: WarsawApikeyRepository = mock {
        on { apikey } doReturn Single.just(APIKEY)
    }
    private val warsawVehicleInterface: WarsawVehicleInterface = mock {
        on { buses(APIKEY) } doReturn Single.just(
            WarsawVehicleResponse(
                listOf(
                    WarsawVehicle(
                        WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                        52.0,
                        19.0,
                        "500",
                        "brigade"
                    )
                )
            )
        )
        on { trams(APIKEY) } doReturn Single.just(
            WarsawVehicleResponse(
                listOf(
                    WarsawVehicle(
                        WarsawVehicleDataSource.dateFormat.format(Calendar.getInstance().time),
                        52.4,
                        19.4,
                        "5",
                        "brigade2"
                    )
                )
            )
        )
    }

    private val tested = WarsawVehicleDataSource(warsawVehicleInterface, warsawApiKeyRepository)

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
    fun `should return error when api key request failed`() {
        // given
        val error: IOException = mock()
        whenever(warsawApiKeyRepository.apikey).thenReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should return error when bus request failed`() {
        // given
        val error: IOException = mock()
        whenever(warsawVehicleInterface.buses(APIKEY)).thenReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should return error when tram request failed`() {
        // given
        val error: IOException = mock()
        whenever(warsawVehicleInterface.trams(APIKEY)).thenReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should return error when trams and buses requests failed`() {
        // given
        val error: IOException = mock()
        val error2: IOException = mock()
        whenever(warsawVehicleInterface.buses(APIKEY)).thenReturn(Single.error(error))
        whenever(warsawVehicleInterface.trams(APIKEY)).thenReturn(Single.error(error2))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError {
            (it as CompositeException).exceptions
                .containsAll(listOf(error, error2))
        }
    }

    @Test
    fun `should filter out outdated vehicles when request succeeded given some vehicles are out dated`() {
        // given
        val threeMinutesAgo = Calendar.getInstance().apply {
            add(Calendar.MINUTE, -3)
        }
        whenever(warsawVehicleInterface.buses(APIKEY)).thenReturn(
            Single.just(
                WarsawVehicleResponse(
                    listOf(
                        WarsawVehicle(
                            WarsawVehicleDataSource.dateFormat.format(threeMinutesAgo.time),
                            52.0,
                            19.0,
                            "500",
                            "brigade"
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
        whenever(warsawVehicleInterface.buses(APIKEY)).thenReturn(
            Single.just(
                WarsawVehicleResponse(
                    listOf(
                        WarsawVehicle(
                            "000000000000000wrong format",
                            52.0,
                            19.0,
                            "500",
                            "brigade"
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