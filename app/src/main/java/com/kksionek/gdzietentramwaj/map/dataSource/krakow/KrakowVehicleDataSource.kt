package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleTransformer

class KrakowVehicleDataSource(
    private val krakowTramInterface: KrakowTramInterface,
    private val krakowBusInterface: KrakowBusInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        Observable.mergeDelayError(
            krakowBusInterface.buses()
                .compose(transformer)
                .flatMapObservable { Observable.fromIterable(it) },
            krakowTramInterface.trams()
                .compose(transformer)
                .flatMapObservable { Observable.fromIterable(it) }
        ).toList()

    private val transformer: SingleTransformer<KrakowVehicleResponse, List<VehicleData>> =
        SingleTransformer { upstream ->
            upstream.map { response ->
                response.vehicles
                    .filter { !it.isDeleted }
                    .map {
                        VehicleData(
                            it.id,
                            it.latLng,
                            it.line,
                            it.isTram()
                        )
                    }
            }
        }
}