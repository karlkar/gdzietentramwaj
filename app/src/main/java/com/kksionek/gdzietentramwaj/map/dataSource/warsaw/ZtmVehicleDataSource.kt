package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Observable
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ZtmVehicleDataSource(
    private val ztmVehicleInterface: ZtmVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        Observable.mergeDelayError(
            ztmVehicleInterface.buses()
                .filterOutOutdated()
                .flatMapObservable { Observable.fromIterable(it) },
            ztmVehicleInterface.trams()
                .filterOutOutdated()
                .flatMapObservable { Observable.fromIterable(it) }
        )
            .map {
                VehicleData(
                    it.id,
                    it.latLng,
                    it.firstLine,
                    it.isTram(),
                    it.brigade
                )
            }
            .toList()

    private fun Single<ZtmVehicleResponse>.filterOutOutdated() =
        map { result ->
            val refDate = Calendar.getInstance()
                .apply { add(Calendar.MINUTE, -2) }
                .let { dateFormat.format(it.time) }
            result.list.filter { refDate <= it.time }
        }

    companion object {

        private val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )
    }
}