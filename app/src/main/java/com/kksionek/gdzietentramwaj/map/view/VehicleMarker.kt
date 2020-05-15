package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.graphics.Color
import androidx.annotation.UiThread
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.model.FollowedTramData
import com.kksionek.gdzietentramwaj.map.model.VehicleToDrawData

private const val POLYLINE_WIDTH = 8F

class VehicleMarker(
    val id: String,
    finalPosition: LatLng,
    prevPosition: LatLng?,
    val marker: Marker,
    val trail: Polyline
) {
    var finalPosition: LatLng = finalPosition
        private set
    var prevPosition: LatLng = prevPosition ?: finalPosition
        private set

    @UiThread
    fun remove() {
        marker.remove()
        trail.remove()
    }

    fun update(vehicleToDraw: VehicleToDrawData) {
        finalPosition = vehicleToDraw.position
        prevPosition = vehicleToDraw.prevPosition ?: vehicleToDraw.position
    }

    object Factory {

        @UiThread
        fun createMarker(
            context: Context,
            map: GoogleMap,
            tramData: VehicleToDrawData,
            isOldIconSetEnabled: Boolean,
            polylineGenerator: PolylineGenerator,
            bitmapCache: BitmapCache
        ): VehicleMarker {
            return VehicleMarker(
                tramData.id,
                tramData.position,
                tramData.prevPosition,
                createMarker(context, bitmapCache, map, tramData, isOldIconSetEnabled),
                createTrail(tramData, map, polylineGenerator)
            )
        }

        @UiThread
        private fun createMarker(
            context: Context,
            bitmapCache: BitmapCache,
            map: GoogleMap,
            vehicleToDrawData: VehicleToDrawData,
            isOldIconSetEnabled: Boolean
        ): Marker {
            val title = context.getString(R.string.marker_info_line, vehicleToDrawData.line)
            val snippet = context.getString(R.string.marker_info_brigade, vehicleToDrawData.brigade)

            return map.addMarker(
                MarkerOptions().apply {
                    position(vehicleToDrawData.position) // if the markers blink - this is the reason - prevPosition should be here, but then new markers appear at the previous position instead of final
                    title(title)
                    snippet(snippet)
                    icon(
                        bitmapCache.getBitmap(
                            vehicleToDrawData.line,
                            vehicleToDrawData.isTram,
                            isOldIconSetEnabled
                        )
                    )
                    if (!isOldIconSetEnabled) {
                        anchor(0.5f, 0.8f)
                    }
                }
            ).apply {
                tag = FollowedTramData(
                    vehicleToDrawData.id,
                    title,
                    snippet,
                    vehicleToDrawData.position
                )
            }
        }

        @UiThread
        private fun createTrail(
            vehicleToDrawData: VehicleToDrawData,
            map: GoogleMap,
            polylineGenerator: PolylineGenerator
        ): Polyline {
            val newPoints = polylineGenerator.generatePolylinePoints(
                vehicleToDrawData.position,
                vehicleToDrawData.prevPosition
            )
            return map.addPolyline(
                PolylineOptions()
                    .color(Color.argb(255, 236, 57, 57))
                    .width(POLYLINE_WIDTH)
            ).apply { points = newPoints }
        }
    }
}
