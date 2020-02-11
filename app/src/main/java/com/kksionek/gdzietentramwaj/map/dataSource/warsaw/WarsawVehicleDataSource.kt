package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import androidx.annotation.VisibleForTesting
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Observable
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WarsawVehicleDataSource(
    private val warsawVehicleInterface: WarsawVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        warsawVehicleInterface.vehicles()
            .filterOutOutdated()
            .flatMapObservable { Observable.fromIterable(it) }
            .map {
                VehicleData(
                    it.id,
                    it.latLng,
                    it.line,
                    it.isTram,
                    it.brigade,
                    it.prevLatLng
                )
            }
            .toList()

    private fun Single<WarsawVehicleResponse>.filterOutOutdated() =
        map { result ->
            val refDate = Calendar.getInstance() // TODO: Don't use Calendar
                .apply { add(Calendar.MINUTE, -2) }
                .let { dateFormat.format(it.time) }
            result.list.filter { refDate <= it.timestamp }
        }

    companion object {

        @VisibleForTesting
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )
    }
}