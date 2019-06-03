package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleData
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSource
import io.reactivex.Observable
import io.reactivex.Single

class GopVehicleDataSource(
    private val gopVehicleInterface: GopVehicleInterface
) : VehicleDataSource {

    private var routesSource = createResourcesSource()

    private fun createResourcesSource(): Single<Map<Type, List<Int>>> = gopVehicleInterface.getRoutes()
        .map { result ->
            val buses = busPattern.findAll(result)
                .map { it.groupValues[1] }
                .map { matchResult -> pattern.findAll(matchResult)
                    .map { it.groupValues[1] }
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }
                    .toList() }
                .first()
            val trams = tramPattern.findAll(result)
                .map { it.groupValues[1] }
                .map { matchResult -> pattern.findAll(matchResult)
                    .map { it.groupValues[1] }
                    .filter { it.isNotEmpty() }
                    .map { it.toInt() }
                    .toList() }
                .first()
            return@map mapOf(
                Type.BUS to buses,
                Type.TRAM to trams
            )
        }.cache()

    override fun vehicles(): Single<List<VehicleData>> {
        return routesSource
            .onErrorResumeNext {
                routesSource = createResourcesSource()
                routesSource
            }
            .flatMapObservable { map: Map<Type, List<Int>> ->
                Observable.mergeDelayError(
                    Observable.fromIterable(map[Type.BUS])
                        .flatMap { gopVehicleInterface.vehiclesA(it).toObservable() },
                    Observable.fromIterable(map[Type.TRAM])
                        .flatMap { gopVehicleInterface.vehiclesT(it).toObservable() }
                )
                    .flatMap { Observable.fromIterable(it.features) }
                    .map {
                        VehicleData(
                            id = it.id.toString(),
                            line = it.properties.line,
                            isTram = it.properties.code == "T",
                            position = fromPointToLatLng(
                                it.geometry.coordinates[0],
                                it.geometry.coordinates[1]
                            ),
                            brigade = it.properties.brigade
                        )
                    }
            }.toList()
    }

    private enum class Type {
        BUS,
        TRAM
    }

    companion object {

        private const val originShift: Double = 2.0 * Math.PI * 6378137.0 / 2.0
        private const val modifier = 180.0 / Math.PI
        private const val modifier2 = Math.PI / 180.0
        private const val modifier3 = Math.PI / 2.0
        private fun fromPointToLatLng(pointX: Double, pointY: Double): LatLng {
            val lon = (pointX / originShift) * 180.0
            val lat =
                modifier * (2.0 * Math.atan(Math.exp(((pointY / originShift) * 180.0) * modifier2)) - modifier3)
            return LatLng(lat, lon)
        }

        private val busPattern = "<h2>Autobus(.*?)<\\/div>".toRegex(RegexOption.DOT_MATCHES_ALL)
        private val tramPattern = "<h2>Tramwaj(.*?)<\\/div>".toRegex(RegexOption.DOT_MATCHES_ALL)
        private val pattern = "href=\"\\/web\\/ml\\/line\\/(.*?)\">".toRegex()
    }
}