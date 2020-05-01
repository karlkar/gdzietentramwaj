package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import androidx.annotation.VisibleForTesting
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Observable
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

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
            val refDate = dateFormat.format(LocalDateTime.now().minusMinutes(2))
            result.list.filter { refDate <= it.timestamp }
        }

    companion object {

        @VisibleForTesting
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}