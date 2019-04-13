package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MpkVehicleDataSource(
    private val mpkVehicleInterface: MpkVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        mpkVehicleInterface.buses()
            .map { mpkVehicle ->
                val refDate = Calendar.getInstance()
                    .apply { add(Calendar.MINUTE, -2) }
                    .let { dateFormat.format(it.time) }
                mpkVehicle.result.records
                    .filter { it.line != "None" }
                    .filter { refDate <= it.timestamp }
            }
            .map { list ->
                list.map {
                    VehicleData(
                        it.id.toString(),
                        it.latLng,
                        it.line,
                        it.isTram(),
                        it.brigade
                    )
                }
            }

    companion object {

        private val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )
    }
}