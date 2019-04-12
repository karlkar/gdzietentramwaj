package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Observable
import io.reactivex.Single

class TtssVehicleDataSource(
    private val ttssTramInterface: TtssTramInterface,
    private val ttssBusInterface: TtssBusInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        Observable.mergeDelayError(
            ttssBusInterface.buses()
                .map { response ->
                    response.vehicles
                        .filter { !it.isDeleted }
                        .map { it.toVehicleData(response.lastUpdate.toString()) }
                }
                .flatMapObservable { Observable.fromIterable(it) },
            ttssTramInterface.trams()
                .map { response ->
                    response.vehicles
                        .filter { !it.isDeleted }
                        .map { it.toVehicleData(response.lastUpdate.toString()) }
                }
                .flatMapObservable { Observable.fromIterable(it) }

        )
            .toList()

//    override fun buses(): Single<List<VehicleData>> = ttssBusInterface
//        .buses()
//        .map { response ->
//            response.vehicles
//                .filter { !it.isDeleted }
//                .map { it.toVehicleData(response.lastUpdate.toString()) }
//        }
//
//    override fun trams(): Single<List<VehicleData>> = ttssTramInterface
//        .trams()
//        .map { response ->
//            response.vehicles
//                .filter { !it.isDeleted }
//                .map { it.toVehicleData(response.lastUpdate.toString()) }
//        }
}