package com.kksionek.gdzietentramwaj.view

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil

private const val MAX_DISTANCE = 150.0
private const val MAX_DISTANCE_RAD = MAX_DISTANCE / 6371000

class PolylineGenerator {

    fun generatePolylinePoints(
        finalPosition: LatLng?,
        prevPosition: LatLng?
    ): List<LatLng> {
        val pointsList = mutableListOf<LatLng>()
        if (finalPosition == null) {
            return pointsList.also { prevPosition?.let { pointsList.add(it) } }
        } else if (prevPosition == null) {
            return pointsList.also { pointsList.add(finalPosition) }
        }

        if (SphericalUtil.computeDistanceBetween(
                finalPosition,
                prevPosition
            ) < MAX_DISTANCE
        ) {
            pointsList.add(prevPosition)
        } else {
            pointsList.add(
                computePointInDirection(
                    finalPosition,
                    prevPosition
                )
            )
        }
        pointsList.add(finalPosition)
        return pointsList
    }

    private fun computePointInDirection(dst: LatLng, src: LatLng): LatLng {
        val brng = Math.toRadians(bearing(dst, src))
        val lat1 = Math.toRadians(dst.latitude)
        val lon1 = Math.toRadians(dst.longitude)

        val lat2 = Math.asin(
            Math.sin(lat1) * Math.cos(MAX_DISTANCE_RAD) + Math.cos(lat1) * Math.sin(
                MAX_DISTANCE_RAD
            ) * Math.cos(brng)
        )
        val a = Math.atan2(
            Math.sin(brng) * Math.sin(MAX_DISTANCE_RAD) * Math.cos(lat1),
            Math.cos(MAX_DISTANCE_RAD) - Math.sin(lat1) * Math.sin(lat2)
        )
        var lon2 = lon1 + a

        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI
        return LatLng(Math.toDegrees(lat2), Math.toDegrees(lon2))
    }

    private fun bearing(src: LatLng, dst: LatLng): Double {
        val degToRad = Math.PI / 180.0
        val phi1 = src.latitude * degToRad
        val phi2 = dst.latitude * degToRad
        val lam1 = src.longitude * degToRad
        val lam2 = dst.longitude * degToRad

        return Math.atan2(
            Math.sin(lam2 - lam1) * Math.cos(phi2),
            Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(
                lam2 - lam1
            )
        ) * 180 / Math.PI
    }
}