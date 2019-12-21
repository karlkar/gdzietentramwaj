package com.kksionek.gdzietentramwaj.map.dataSource.szczecin


import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single

class SzczecinVehicleDataSource(
    private val szczecinVehicleInterface: SzczecinVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        szczecinVehicleInterface.vehicles()
            .map { list ->
                list.map {
                    VehicleData(
                        id = it.id,
                        position = LatLng(it.lat.toDouble(), it.lng.toDouble()),
                        line = it.line,
                        isTram = it.lineType[0] == 't',
                        brigade = it.brigade
                    )
                }
            }
}