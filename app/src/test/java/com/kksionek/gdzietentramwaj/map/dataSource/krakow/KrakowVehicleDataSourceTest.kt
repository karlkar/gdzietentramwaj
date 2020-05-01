package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.Instant

class KrakowVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val krakowTramInterface: KrakowTramInterface = mock {
        on { trams() } doReturn Single.just(
            KrakowVehicleResponse(
                Instant.now().toEpochMilli(),
                listOf(KrakowVehicle(false, "1", "na", 3600000, 2 * 3600000))
            )
        )
    }
    private val krakowBusInterface: KrakowBusInterface = mock {
        on { buses() } doReturn Single.just(
            KrakowVehicleResponse(
                Instant.now().toEpochMilli(),
                listOf(KrakowVehicle(false, "2", "name2", 3 * 3600000, 4 * 3600000))
            )
        )
    }

    private lateinit var tested: KrakowVehicleDataSource

    private fun initialize() {
        tested = KrakowVehicleDataSource(
            krakowTramInterface,
            krakowBusInterface
        )
    }

    @Test
    fun `should return vehicle list when request successful`() {
        // given
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 2
                        && !it[0].isTram
                        && it[0].line == "name2"
                        && it[0].id == "2"
                        && it[1].isTram
                        && it[1].line == "na"
                        && it[1].id == "1"
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should filter out deleted vehicles when request contains deleted vehicles`() {
        // given
        whenever(krakowTramInterface.trams()).thenReturn(
            Single.just(
                KrakowVehicleResponse(
                    Instant.now().toEpochMilli(),
                    listOf(KrakowVehicle(true, "1", "na", 3600000, 2 * 3600000))
                )
            )
        )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.size == 1
                    && !it[0].isTram
                    && it[0].line == "name2"
                    && it[0].id == "2"}
            .assertNoErrors()
            .assertComplete()
    }
}