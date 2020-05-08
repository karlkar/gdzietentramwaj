package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.annotation.UiThread
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.model.VehicleToDrawData
import com.kksionek.gdzietentramwaj.map.viewModel.FollowedTramData

class TramMarker(
    context: Context,
    map: GoogleMap,
    tramData: VehicleToDrawData,
    isOldIconSetEnabled: Boolean,
    polylineGenerator: PolylineGenerator
) {

    val id = tramData.id

    var finalPosition: LatLng = tramData.position
        private set
    var prevPosition: LatLng = tramData.prevPosition ?: tramData.position
        private set

    val marker: Marker = createMarker(context, map, tramData, isOldIconSetEnabled)

    var trail: Polyline = createTrail(tramData, map, polylineGenerator)

    @UiThread
    fun remove() {
        marker.remove()
        trail.remove()
    }

    fun update(vehicleToDraw: VehicleToDrawData) {
        finalPosition = vehicleToDraw.position
        prevPosition = vehicleToDraw.prevPosition ?: vehicleToDraw.position
    }

    private fun createMarker(
        context: Context,
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
                    TramMarker.getBitmap( // TODO: Extract to non static class
                        vehicleToDrawData.line,
                        vehicleToDrawData.isTram,
                        context,
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
                .width(TramMarker.POLYLINE_WIDTH)
        ).apply { points = newPoints }
    }

    companion object {

        const val POLYLINE_WIDTH = 8F

        private val bitmapCache = LruCache<String, BitmapDescriptor>(50)

        fun getBitmap(
            line: String,
            isTram: Boolean,
            context: Context,
            isOldIconSetEnabled: Boolean
        ): BitmapDescriptor {
            val descriptor = bitmapCache[line]
            return if (descriptor == null) {
                val bitmap = createBitmap(context, line, isTram, isOldIconSetEnabled)
                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                bitmapCache.put(line, bitmapDescriptor)
                bitmapDescriptor
            } else {
                descriptor
            }
        }

        private fun createBitmap(
            context: Context,
            line: String,
            isTram: Boolean,
            isOldIconEnabled: Boolean
        ): Bitmap? {
            val textColor = getTextColor(line)
            val layoutRes = getTramIcon(isOldIconEnabled, isTram)
            val view = LayoutInflater.from(context).inflate(layoutRes, null)

            val textview = view.findViewById<TextView>(R.id.marker_textview)
            textview.text = line
            textview.setTextColor(textColor)

            val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(measureSpec, measureSpec)
            val measuredWidth = view.measuredWidth
            val measuredHeight = view.measuredHeight

            view.layout(0, 0, measuredWidth, measuredHeight)

            val bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
                .apply { eraseColor(Color.TRANSPARENT) }

            Canvas(bitmap).let { view.draw(it) }
            return bitmap
        }

        private fun getTextColor(line: String) = when {
            line.length < 3 -> Color.BLACK
            line.startsWith("4")
                    || line.startsWith("5")
                    || line.startsWith("E") -> Color.RED
            line.startsWith("L") -> Color.rgb(76, 165, 80)
            else -> Color.BLACK
        }

        private fun getTramIcon(isOldIconEnabled: Boolean, isTram: Boolean) =
            if (isOldIconEnabled) {
                if (isTram) R.layout.ic_marker_old_tram else R.layout.ic_marker_old_bus
            } else {
                if (isTram) R.layout.ic_marker_new_tram else R.layout.ic_marker_new_bus
            }

        fun clearCache() {
            bitmapCache.evictAll()
        }
    }
}
