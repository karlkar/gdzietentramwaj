package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.exceptions.CompositeException
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val ROUTES_RESPONSE = """
        <h2>Autobus</h2>
        <ul data-role="listview" data-filter="true" data-filter-placeholder="Filtruj...">
            <li><a data-ajax="false"
                    href="/web/ml/line/55">0</a></li>
        </ul>
    </div><div data-role="collapsible">
        <h2>Tramwaj - TEST</h2>
        <ul data-role="listview" data-filter="true" data-filter-placeholder="Filtruj...">
            <li><a data-ajax="false" href="/web/ml/line/486">1</a></li>
        </ul>
    </div>
</div>
</div></div>"""

class GopVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val gopVehicleInterface: GopVehicleInterface = mock {
        on { getRoutes() } doReturn Single.just(ROUTES_RESPONSE)
        on { vehiclesA(any()) } doReturn Single.just(
            GopVehicleList(
                listOf(
                    GopFeature(
                        GopGeometry(
                            listOf(1.0, 2.0)
                        ),
                        1,
                        GopProperties("A", "0", "brigade")
                    )
                )
            )
        )
        on { vehiclesT(any()) } doReturn Single.just(
            GopVehicleList(
                listOf(
                    GopFeature(
                        GopGeometry(
                            listOf(1.0, 2.0)
                        ),
                        2,
                        GopProperties("T", "1", "brigade")
                    )
                )
            )
        )
        on { vehiclesTB(any()) } doReturn Single.just(GopVehicleList(emptyList()))
        on { vehiclesUNK(any()) } doReturn Single.just(GopVehicleList(emptyList()))
    }

    private lateinit var tested: GopVehicleDataSource

    private fun initialize() {
        tested = GopVehicleDataSource(gopVehicleInterface)
    }

    @Test
    fun `should return vehicles when request succeeds`() {
        // given
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 2
                        && it[0].id == "1"
                        && !it[0].isTram
                        && it[0].line == "0"
                        && it[1].id == "2"
                        && it[1].isTram
                        && it[1].line == "1"
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should recreate routes list when error occurred during first routes request`() {
        // given
        val error: IOException = mock()
        whenever(gopVehicleInterface.getRoutes()).thenReturn(
            Single.error(error),
            Single.just(ROUTES_RESPONSE)
        )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 2
                        && it[0].id == "1"
                        && !it[0].isTram
                        && it[0].line == "0"
                        && it[1].id == "2"
                        && it[1].isTram
                        && it[1].line == "1"
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return composite exception when two errors happened getting vehicles`() {
        // given
        val error: IOException = mock()
        val error2: IOException = mock()
        whenever(gopVehicleInterface.vehiclesA(any())).thenReturn(Single.error(error))
        whenever(gopVehicleInterface.vehiclesT(any())).thenReturn(Single.error(error2))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertNoValues()
            .assertError {
                it is CompositeException
                        && it.exceptions.size == 2
                        && it.exceptions[0] === error
                        && it.exceptions[1] === error2
            }
    }

    @Test
    fun `should return exception when one error happened getting vehicles`() { // TODO: Maybe it shouldn't?
        // given
        val error: IOException = mock()
        whenever(gopVehicleInterface.vehiclesA(any())).thenReturn(Single.error(error))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertNoValues()
            .assertError { it === error }
    }

    @Test
    fun `should return exception when incorrect routes obtained`() {
        // given
        whenever(gopVehicleInterface.getRoutes()).thenReturn(Single.just(""))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertNoValues()
            .assertError { it is NoSuchElementException } // TODO: Should log to crashlytics?
    }
}