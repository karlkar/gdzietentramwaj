package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.kksionek.gdzietentramwaj.R

class BitmapCacheImpl(appContext: Context): BitmapCache {

    private val bitmapCache = LruCache<String, BitmapDescriptor>(50)
    private val layoutInflater: LayoutInflater = LayoutInflater.from(appContext)

    override fun getBitmap(
        line: String,
        isTram: Boolean,
        isOldIconSetEnabled: Boolean
    ): BitmapDescriptor {
        val descriptor = bitmapCache[line]
        return if (descriptor == null) {
            val bitmap = createBitmap(line, isTram, isOldIconSetEnabled)
            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
            bitmapCache.put(line, bitmapDescriptor)
            bitmapDescriptor
        } else {
            descriptor
        }
    }

    private fun createBitmap(
        line: String,
        isTram: Boolean,
        isOldIconEnabled: Boolean
    ): Bitmap? {
        val textColor = getTextColor(line)
        val layoutRes = getVehicleIcon(isOldIconEnabled, isTram)
        val view = layoutInflater.inflate(layoutRes, null)

        view.findViewById<TextView>(R.id.marker_textview).apply {
            text = line
            setTextColor(textColor)
        }

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

    // TODO: This may differ in different cities...
    private fun getTextColor(line: String) = when {
        line.length < 3 -> Color.BLACK
        line.startsWith("4")
                || line.startsWith("5")
                || line.startsWith("E") -> Color.RED
        line.startsWith("L") -> Color.rgb(76, 165, 80)
        else -> Color.BLACK
    }

    private fun getVehicleIcon(isOldIconEnabled: Boolean, isTram: Boolean) =
        if (isOldIconEnabled) {
            if (isTram) R.layout.ic_marker_old_tram else R.layout.ic_marker_old_bus
        } else {
            if (isTram) R.layout.ic_marker_new_tram else R.layout.ic_marker_new_bus
        }

    override fun clearCache() {
        bitmapCache.evictAll()
    }
}