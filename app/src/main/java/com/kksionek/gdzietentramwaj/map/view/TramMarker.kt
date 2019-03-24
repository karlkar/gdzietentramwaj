package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Typeface.BOLD
import android.graphics.drawable.Drawable
import android.support.annotation.UiThread
import android.support.v4.content.ContextCompat
import android.util.LruCache
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.dataSource.TramData


class TramMarker @UiThread constructor(tramData: TramData) {

    val tramLine: String = tramData.firstLine

    var finalPosition: LatLng = tramData.latLng
        private set

    val id = tramData.id

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

    fun updatePosition(finalPosition: LatLng) {
        if (finalPosition === this.finalPosition)
            _prevPosition = null
        else {
            _prevPosition = this.finalPosition
            this.finalPosition = finalPosition
        }
    }

    companion object {

        const val POLYLINE_WIDTH = 8F

        private val mBitmaps = LruCache<String, BitmapDescriptor>(50)

        private fun drawableToBitmap(drawable: Drawable): Bitmap {
            var bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            if (!bitmap.isMutable) { // On Galaxy ACE immutable map is returned
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }

        @JvmStatic
        fun getBitmap(
            line: String,
            context: Context
        ): BitmapDescriptor {
            val descriptor = mBitmaps[line]
            if (descriptor == null) {
                val isTram = checkIfIsTram(line)
                val textColor = when {
                    line.length < 3 -> Color.BLACK
                    line.startsWith("4")
                            || line.startsWith("5")
                            || line.startsWith("E") -> Color.RED
                    line.startsWith("L") -> Color.rgb(76, 165, 80)
                    else -> Color.BLACK
                }
                val clustersPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = textColor
                    textSize = 35.0f
                    typeface = Typeface.create(typeface, BOLD)
                    textAlign = Paint.Align.CENTER
                }
                val icon = if (isTram) R.drawable.ic_tram else R.drawable.ic_bus
                val drawable = ContextCompat.getDrawable(context, icon)
                val bitmap = drawableToBitmap(drawable!!)

                val modifier = if (isTram) 2.15f else 2.8f
                val canvas = Canvas(bitmap)
                canvas.drawText(
                    line,
                    canvas.width / 2.0f,
                    canvas.height / modifier - (clustersPaint.fontMetrics.ascent + clustersPaint.fontMetrics.descent) / 2.0f,
                    clustersPaint
                )

                val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                mBitmaps.put(line, bitmapDescriptor)
                return bitmapDescriptor
            } else {
                return descriptor
            }
        }

        private fun checkIfIsTram(line: String) = line.length < 3
    }
}
