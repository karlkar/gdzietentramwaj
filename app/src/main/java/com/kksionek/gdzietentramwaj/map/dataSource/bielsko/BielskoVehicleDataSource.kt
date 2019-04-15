package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Single
import java.util.Calendar

class BielskoVehicleDataSource(
    private val bielskoVehicleInterface: BielskoVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> {
        val refDate = Calendar.getInstance()
            .apply { add(Calendar.MINUTE, -2) }
            .timeInMillis
        return bielskoVehicleInterface.vehicles()
            .map {
                it.list
                    .filter { it.timestamp > refDate }
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