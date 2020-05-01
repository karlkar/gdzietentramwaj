package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.math.abs

class LodzVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val lodzVehicleInterface: LodzVehicleInterface = mock {
        on { vehicles() } doReturn Single.just("""<p>[4201, 4201, "15   ", "4", "T", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>""")
    }

    private lateinit var tested: LodzVehicleDataSource

    private fun initialize() {
        tested = LodzVehicleDataSource(lodzVehicleInterface)
    }

    @Test
    fun `should return a vehicle list when request succeeded`() {
        // given
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.isNotEmpty()
                        && it[0].id == "4201"
                        && abs(it[0].position.latitude - 51.77618) < 0.00001
                        && abs(it[0].position.longitude - 19.44865) < 0.00001
                        && it[0].line == "15"
                        && it[0].isTram
                        && it[0].brigade == "4"
                        && abs(it[0].prevPosition!!.latitude - 51.77612) < 0.00001
                        && abs(it[0].prevPosition!!.longitude - 19.44794) < 0.00001
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return empty list when incorrect response received`() {
        // given
        whenever(lodzVehicleInterface.vehicles()).thenReturn(Single.just(""))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(lodzVehicleInterface.vehicles()).thenReturn(Single.error(error))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertError { it === error }
    }

    @Test
    fun `should return error when request has no proper line name info`() {
        // given
        whenever(lodzVehicleInterface.vehicles())
            .thenReturn(
                Single.just(//       here   VV
                    """<p>[4201, 4201, "", "4", "T", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>"""
                )
            )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test // TODO: Should report error to crashlytics
    fun `should return error when response has incorrect format`() {
        // given
        whenever(lodzVehicleInterface.vehicles()).thenReturn(Single.just("""<p>[]</p>"""))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertError { it is IndexOutOfBoundsException }
    }

    @Test // TODO: Should report error to crashlytics
    fun `should return error when response has too little fields`() {
        // given
        whenever(lodzVehicleInterface.vehicles()).thenReturn(Single.just("""<p>[4201, 4201, "15   "]</p>"""))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertError { it is IndexOutOfBoundsException }
    }

    @Test // TODO: Should report error to crashlytics
    fun `should return error when response has wrong format of latitude`() {
        // given
        whenever(lodzVehicleInterface.vehicles())
            .thenReturn(
                Single.just( //  here                                               VV
                    """<p>[4201, 4201, "15   ", "4", "T", 12902, 19, 315, 94, "A", 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>"""
                )
            )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertError { it is NumberFormatException }
    }

    @Test
    fun `should return vehicle list when response has wrong format of name`() {
        // given
        whenever(lodzVehicleInterface.vehicles())
            .thenReturn(
                Single.just( //  here        VV
                    """<p>[4201, 4201, 15, "4", "T", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>"""
                )
            )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.size == 1 && it[0].line == "15" }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return tram in vehicle list when response contains tram`() {
        // given
        whenever(lodzVehicleInterface.vehicles())
            .thenReturn(
                Single.just(
                    """<p>[4201, 4201, "15   ", "4", "T", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>"""
                )
            )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.size == 1 && it[0].isTram }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return bus in vehicle list when response contains bus`() {
        // given
        whenever(lodzVehicleInterface.vehicles())
            .thenReturn(
                Single.just(
                    """<p>[4201, 4201, "15   ", "4", "B", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "B", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>"""
                )
            )
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.size == 1 && !it[0].isTram }
            .assertNoErrors()
            .assertComplete()
    }
}