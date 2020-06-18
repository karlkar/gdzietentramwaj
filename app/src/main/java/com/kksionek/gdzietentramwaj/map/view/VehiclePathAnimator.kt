package com.kksionek.gdzietentramwaj.map.view

import android.animation.ValueAnimator
import androidx.annotation.UiThread
import com.google.maps.android.SphericalUtil

const val ANIMATION_DURATION = 3000L

class VehiclePathAnimator(private val polylineGenerator: PolylineGenerator) {

    private val animationMarkers = mutableListOf<VehicleMarker>()

    private val animatorUpdateListener =
        ValueAnimator.AnimatorUpdateListener { animation ->
            val fraction = animation?.animatedFraction ?: return@AnimatorUpdateListener

            val vehicleMarkerList: List<VehicleMarker> = animationMarkers.toList()
            for (vehicleMarker in vehicleMarkerList) {
                val prevPosition = vehicleMarker.prevPosition
                if (prevPosition == vehicleMarker.finalPosition) {
                    continue
                }
                val intermediatePos = SphericalUtil.interpolate(
                    prevPosition,
                    vehicleMarker.finalPosition,
                    fraction.toDouble()
                )
                vehicleMarker.marker.position = intermediatePos

                val curPointsList = vehicleMarker.trail.points
                val prevPos = if (curPointsList.size != 0) {
                    curPointsList[0]
                } else {
                    prevPosition
                }
                val pointsList = polylineGenerator.generatePolylinePoints(intermediatePos, prevPos)
                vehicleMarker.trail.points = pointsList
            }
        }

    private val valueAnimator = ValueAnimator
        .ofFloat(0F, 1F)
        .setDuration(ANIMATION_DURATION).apply { addUpdateListener(animatorUpdateListener) }

    @UiThread
    fun removeAllMarkers() {
        animationMarkers.clear()
    }

    fun startAnimation() {
        if (animationMarkers.isNotEmpty()) {
            valueAnimator.start()
        }
    }

    @UiThread
    fun addMarker(vehicleMarker: VehicleMarker) {
        animationMarkers.add(vehicleMarker)
    }

    @UiThread
    fun addAllMarkers(iterable: Iterable<VehicleMarker>) {
        animationMarkers.addAll(iterable)
    }

    fun removeMarker(vehicleMarker: VehicleMarker) {
        animationMarkers.remove(vehicleMarker)
    }

    companion object {
        init {
            ValueAnimator.setFrameDelay(180)
        }
    }
}