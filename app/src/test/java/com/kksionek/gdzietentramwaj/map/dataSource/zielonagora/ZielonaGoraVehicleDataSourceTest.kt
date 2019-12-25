package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val VEHICLE_ID = "111"
private const val VEHICLE_LINE = "1"
private const val VEHICLE_LATITUDE = 52.0
private const val VEHICLE_LONGITUDE = 19.0

class ZielonaGoraVehicleDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val zielonaGoraVehicleInterface: ZielonaGoraVehicleInterface = mock {
        on { vehicles() } doReturn Single.just(
            listOf(
                ZielonaGoraVehicle(VEHICLE_ID, VEHICLE_LINE, VEHICLE_LATITUDE, VEHICLE_LONGITUDE)
            )
        )
    }

    private val tested = ZielonaGoraVehicleDataSource(zielonaGoraVehicleInterface)

    @Test
    fun `should return vehicles when requested`() {
        // when
        val observer = tested.vehicles().test()

        // then
        observer
            .assertValue {
                it.size == 1
                        && it[0].id == VEHICLE_ID
                        && it[0].line == VEHICLE_LINE
                        && it[0].brigade == null
                        && it[0].position == LatLng(VEHICLE_LATITUDE, VEHICLE_LONGITUDE)
                        && it[0].prevPosition == null
                        && !it[0].isTram
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when failed`() {
        // given
        val error: IOException = mock()
        whenever(zielonaGoraVehicleInterface.vehicles()).thenReturn(Single.error(error))

        // when
        val observer = tested.vehicles().test()

        // then
        observer.assertError { it === error }
    }
}