package com.kksionek.gdzietentramwaj.map.view

import android.animation.ValueAnimator
import android.support.annotation.UiThread
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.maps.android.SphericalUtil

class TramPathAnimator(private val polylineGenerator: PolylineGenerator) {

    private val animationMarkers = mutableListOf<TramMarker>()

    private val animatorUpdateListener =
        ValueAnimator.AnimatorUpdateListener { animation ->
            val fraction = animation?.animatedFraction ?: return@AnimatorUpdateListener
            var marker: Marker
            var polyline: Polyline
            var prevPosition: LatLng
            var intermediatePos: LatLng

            val immutableList: List<TramMarker> = animationMarkers.toList()
            for (tramMarker in immutableList) {
                marker = tramMarker.marker ?: continue
                polyline = tramMarker.polyline ?: continue
                prevPosition = tramMarker.prevPosition ?: continue
                intermediatePos = SphericalUtil.interpolate(
                    prevPosition,
                    tramMarker.finalPosition,
                    fraction.toDouble()
                )
                marker.position = intermediatePos

                val curPointsList = polyline.points
                val prevPos = if (curPointsList.size != 0) {
                    curPointsList[0]
                } else {
                    prevPosition
                }
                val pointsList = polylineGenerator.generatePolylinePoints(intermediatePos, prevPos)
                polyline.points = pointsList
            }
        }

    private val valueAnimator = ValueAnimator
        .ofFloat(0F, 1F)
        .setDuration(3000).apply { addUpdateListener(animatorUpdateListener) }

    @UiThread
    fun removeAllAnimatedMarkers() {
        animationMarkers.clear()
    }

    fun startAnimation() {
        if (animationMarkers.isNotEmpty()) {
            valueAnimator.start()
        }
    }

    @UiThread
    fun addMarker(tramMarker: TramMarker) {
        animationMarkers.add(tramMarker)
    }

    fun removeMarker(tramMarker: TramMarker) {
        animationMarkers.remove(tramMarker)
    }

    companion object {
        init {
            ValueAnimator.setFrameDelay(180)
        }
    }
}