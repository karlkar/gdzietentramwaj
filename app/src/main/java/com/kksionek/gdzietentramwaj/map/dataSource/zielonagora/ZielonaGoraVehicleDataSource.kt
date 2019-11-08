package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single

class ZielonaGoraVehicleDataSource(
    private val zielonaGoraVehicleInterface: ZielonaGoraVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        zielonaGoraVehicleInterface.vehicles()
            .map { list ->
                list.map {
                    VehicleData(
                        id = it.id,
                        line = it.label,
                        position = LatLng(it.lat, it.lon),
                        isTram = false
                    )
                }
            }
}