package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.repository.VehicleData
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VehicleZtmInterface(
    private val ztmVehicleInterface: ZtmVehicleInterface
) : VehicleDataSource {

    override fun buses(): Single<List<VehicleData>> =
        ztmVehicleInterface.buses()
            .filterOutOutdated()
            .map { list ->
                list.map { VehicleData(it.id, it.time, it.latLng, it.firstLine, it.brigade) }
            }

    override fun trams(): Single<List<VehicleData>> =
        ztmVehicleInterface.trams()
            .filterOutOutdated()
            .map { list ->
                list.map { VehicleData(it.id, it.time, it.latLng, it.firstLine, it.brigade) }
            }

    private fun Single<TramList>.filterOutOutdated() =
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