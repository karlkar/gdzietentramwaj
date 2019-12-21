package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.math.abs

class SzczecinVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val szczecinVehicleInterface: SzczecinVehicleInterface = mock {
        on { vehicles() } doReturn Single.just(
            listOf(
                SzczecinVehicle(
                    "1",
                    "500",
                    "brigade",
                    "a",
                    "19.44865",
                    "51.77618"
                )
            )
        )
    }

    private lateinit var tested: SzczecinVehicleDataSource

    private fun initialize() {
        tested = SzczecinVehicleDataSource(szczecinVehicleInterface)
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
                it.size == 1
                        && it[0].id == "1"
                        && it[0].line == "500"
                        && !it[0].isTram
                        && it[0].brigade == "brigade"
                        && abs(it[0].position.latitude - 19.44865) < 0.00001
                        && abs(it[0].position.longitude - 51.77618) < 0.00001
                        && it[0].prevPosition == null
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return an error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(szczecinVehicleInterface.vehicles()).thenReturn(Single.error(error))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertError { it === error }
    }

    @Test
    fun `should return an error when request contains invalid latitude`() {
        // given
        whenever(szczecinVehicleInterface.vehicles())
            .thenReturn(
                Single.just(
                    listOf(
                        SzczecinVehicle("1", "500", "brigade", "t", "aaa", "54.1232")
                    )
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
    fun `should return a tram in a vehicle list when response contained tram`() {
        // given
        whenever(szczecinVehicleInterface.vehicles())
            .thenReturn(Single.just(
                listOf(
                    SzczecinVehicle(
                        "1",
                        "5",
                        "brigade",
                        "t",
                        "19.44865",
                        "51.77618"
                    )
                )
            ))
        initialize()

        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue { it.size == 1 && it[0].isTram }
            .assertNoErrors()
            .assertComplete()
    }
}