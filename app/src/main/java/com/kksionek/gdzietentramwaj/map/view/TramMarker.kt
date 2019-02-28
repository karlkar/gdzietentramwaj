package com.kksionek.gdzietentramwaj.map.view

import android.graphics.Color
import android.support.annotation.UiThread
import android.util.LruCache

import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.ui.IconGenerator
import com.kksionek.gdzietentramwaj.base.dataSource.TramData

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

        @JvmStatic
        fun getBitmap(line: String, iconGenerator: IconGenerator): BitmapDescriptor {
            fun createNewBitmapDescriptor(
                line: String,
                iconGenerator: IconGenerator
            ): BitmapDescriptor {
                iconGenerator.setColor(
                    if (isTram(line))
                        Color.argb(255, 249, 245, 206)
                    else
                        Color.WHITE
                )
                val bitmapDescriptor: BitmapDescriptor =
                    BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(line))
                mBitmaps.put(line, bitmapDescriptor)
                return bitmapDescriptor
            }

            return mBitmaps.get(line) ?: createNewBitmapDescriptor(line, iconGenerator)
        }

        private fun isTram(line: String) = line.length < 3
    }
}
