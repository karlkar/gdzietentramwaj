package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.SingleTransformer

class TtssVehicleDataSource(
    private val ttssTramInterface: TtssTramInterface,
    private val ttssBusInterface: TtssBusInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        Observable.mergeDelayError(
            ttssBusInterface.buses()
                .compose(transformer)
                .flatMapObservable { Observable.fromIterable(it) },
            ttssTramInterface.trams()
                .compose(transformer)
                .flatMapObservable { Observable.fromIterable(it) }

        ).toList()

    private val transformer: SingleTransformer<TtssVehicleResponse, List<VehicleData>> =
        SingleTransformer { upstream ->
            upstream.map { response ->
                response.vehicles
                    .filter { !it.isDeleted }
                    .map {
                        VehicleData(
                            it.id,
                            it.latLng,
                            it.line,
                            it.isTram(),
                            null
                        )
                    }
            }
        }
}