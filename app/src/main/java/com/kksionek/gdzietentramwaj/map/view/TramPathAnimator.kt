package com.kksionek.gdzietentramwaj.map.view

import android.animation.ValueAnimator
import androidx.annotation.UiThread
import com.google.maps.android.SphericalUtil

const val ANIMATION_DURATION = 3000L

class TramPathAnimator(private val polylineGenerator: PolylineGenerator) {

    private val animationMarkers = mutableListOf<TramMarker>()

    private val animatorUpdateListener =
        ValueAnimator.AnimatorUpdateListener { animation ->
            val fraction = animation?.animatedFraction ?: return@AnimatorUpdateListener

            val immutableList: List<TramMarker> = animationMarkers.toList()
            for (tramMarker in immutableList) {
                val marker = tramMarker.marker ?: continue
                val polyline = tramMarker.trail ?: continue
                val prevPosition = tramMarker.prevPosition
                if (prevPosition == tramMarker.finalPosition) {
                    continue
                }
                val intermediatePos = SphericalUtil.interpolate(
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
    fun addMarker(tramMarker: TramMarker) {
        animationMarkers.add(tramMarker)
    }

    @UiThread
    fun addAllMarkers(iterable: Iterable<TramMarker>) {
        animationMarkers.addAll(iterable)
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