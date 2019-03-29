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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.dataSource.TramData
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider


class TramMarker(tramData: TramData) {

    val tramLine: String = tramData.firstLine

    var finalPosition: LatLng = tramData.latLng
        private set

    val id = tramData.id

    val brigade = tramData.brigade

    private var _marker: Marker? = null
    var marker: Marker?
        get() = _marker
        set(marker) {
            _marker?.remove()
            _marker = marker
        }

    private var _polyline: Polyline? = null
    var polyline: Polyline?
        get() = _polyline
        set(polyline) {
            _polyline?.remove()
            _polyline = polyline
        }

    private var _prevPosition: LatLng? = null
    val prevPosition: LatLng?
        get() = _prevPosition ?: finalPosition

    @UiThread
    fun remove() {
        _marker?.remove()
        _marker = null

        _polyline?.remove()
        _polyline = null
    }

    fun isOnMap(bounds: LatLngBounds): Boolean = bounds.contains(finalPosition)
            || (_prevPosition?.let { bounds.contains(it) } ?: false)

    fun updatePosition(newFinalPosition: LatLng) {
        if (newFinalPosition === finalPosition)
            _prevPosition = null
        else {
            _prevPosition = finalPosition
            finalPosition = newFinalPosition
        }
    }

    companion object {

        const val POLYLINE_WIDTH = 8F

        private val bitmapCache = LruCache<String, BitmapDescriptor>(50)

        fun getBitmap(
            line: String,
            context: Context,
            iconSettingsProvider: IconSettingsProvider
        ): BitmapDescriptor {
            val descriptor = bitmapCache[line]
            return if (descriptor == null) {
                val bitmap = createBitmap(context, line, iconSettingsProvider.isOldIconSetEnabled())
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
            isOldIconEnabled: Boolean
        ): Bitmap? {
            val textColor = getTextColor(line)
            val isTram = checkIfIsTram(line)
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

        private fun checkIfIsTram(line: String) = line.length < 3

        fun clearCache() {
            bitmapCache.evictAll()
        }
    }
}
