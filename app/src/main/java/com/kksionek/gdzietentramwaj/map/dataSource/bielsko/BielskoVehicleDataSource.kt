package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime

class BielskoVehicleDataSource(
    private val bielskoVehicleInterface: BielskoVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> {
        val zone = ZoneOffset.from(ZonedDateTime.now())
        val refMillis = LocalDateTime.now().minusMinutes(2).toInstant(zone).toEpochMilli()
        return bielskoVehicleInterface.vehicles()
            .map { bielskoVehicleList ->
                bielskoVehicleList.list
                    .filter { it.timestamp > refMillis }
                    .map {
                        VehicleData(
                            id = it.id.toString(),
                            position = LatLng(it.latitude, it.longitude),
                            line = it.line,
                            isTram = it.isTram(),
                            brigade = it.brigade
                        )
                    }
            }
    }
}