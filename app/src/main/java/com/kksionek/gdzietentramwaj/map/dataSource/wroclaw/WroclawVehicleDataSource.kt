package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import androidx.annotation.VisibleForTesting
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import com.kksionek.gdzietentramwaj.map.model.VehicleData
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

class WroclawVehicleDataSource(
    private val wroclawVehicleInterface: WroclawVehicleInterface
) : VehicleDataSource {

    override fun vehicles(): Single<List<VehicleData>> =
        wroclawVehicleInterface.buses()
            .map { mpkVehicle ->
                val refDateTime = dateFormat.format(LocalDateTime.now().minusMinutes(2))
                mpkVehicle.result.records
                    .filter { it.line != "None" }
                    .filter { refDateTime <= it.timestamp }
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
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    }
}