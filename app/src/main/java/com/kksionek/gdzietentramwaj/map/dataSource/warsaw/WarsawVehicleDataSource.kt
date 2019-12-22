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
    private val warsawVehicleInterface: WarsawVehicleInterface,
    private val warsawApikeyRepository: WarsawApikeyRepository
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        warsawApikeyRepository.apikey.flatMap { apikey ->
            Observable.mergeDelayError(
                warsawVehicleInterface.buses(apikey)
                    .filterOutOutdated()
                    .flatMapObservable { Observable.fromIterable(it) },
                warsawVehicleInterface.trams(apikey)
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
        }

    private fun Single<WarsawVehicleResponse>.filterOutOutdated() =
        map { result ->
            val refDate = Calendar.getInstance() // TODO: Don't use Calendar
                .apply { add(Calendar.MINUTE, -2) }
                .let { dateFormat.format(it.time) }
            result.list.filter { refDate <= it.time }
        }

    companion object {

        @VisibleForTesting
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )
    }
}