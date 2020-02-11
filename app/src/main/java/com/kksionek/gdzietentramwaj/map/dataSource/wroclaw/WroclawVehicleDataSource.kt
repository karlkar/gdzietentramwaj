package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import androidx.annotation.VisibleForTesting
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WroclawVehicleDataSource(
    private val wroclawVehicleInterface: WroclawVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        wroclawVehicleInterface.buses()
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

        @VisibleForTesting
        val dateFormat = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US
        )
    }
}