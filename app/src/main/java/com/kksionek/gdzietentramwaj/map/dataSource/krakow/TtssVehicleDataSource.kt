package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.repository.VehicleData
import io.reactivex.Single

class TtssVehicleDataSource(
    private val ttssTramInterface: TtssTramInterface,
    private val ttssBusInterface: TtssBusInterface
) : VehicleDataSource {

    override fun buses(): Single<List<VehicleData>> = ttssBusInterface
        .buses()
        .map { response ->
            response.vehicles
                .filter { !it.isDeleted }
                .map { it.toVehicleData(response.lastUpdate.toString()) }
        }

    override fun trams(): Single<List<VehicleData>> = ttssTramInterface
        .trams()
        .map { response ->
            response.vehicles
                .filter { !it.isDeleted }
                .map { it.toVehicleData(response.lastUpdate.toString()) }
        }
}