package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Single

class LodzVehicleDataSource(
    private val lodzVehicleInterface: LodzVehicleInterface
) : VehicleDataSource {

    // <p>[4201, 4201, "15   ", "4", "T", 12902, 19, 315, 94, 19.44865, 51.77618, 19.44794, 51.77612, -133, "-00:02:13", 1, "20:03", 12903,"21:24","15   ","5","P",0, "T", "NKB","STOKI","CHOJNY KURCZAKI", 177.56]</p>

    override fun vehicles(): Single<List<VehicleData>> =
        lodzVehicleInterface.vehicles()
            .map { rawResponse ->
                pattern.findAll(rawResponse).map { matchResult ->
                    val items = matchResult.groupValues[1].split(",").map { it.replace("\"", "") }
                    val line = items[2].trim()
                    if (line.isEmpty()) {
                        null
                    } else {
                        VehicleData(
                            id = items[0],
                            position = LatLng(items[10].toDouble(), items[9].toDouble()),
                            // prevPosition = LatLng(items[12].toDouble(), items[11].toDouble)
                            line = items[2].trim(),
                            isTram = items[23].trim() == "T",
                            brigade = items[3]
                        )
                    }
                }
                    .filterNotNull()
                    .toList()
            }

    companion object {
        private val pattern = "<p>\\[(.*?)\\]<\\/p>".toRegex()
    }
}