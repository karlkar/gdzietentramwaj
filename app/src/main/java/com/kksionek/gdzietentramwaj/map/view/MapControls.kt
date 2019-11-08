package com.kksionek.gdzietentramwaj.map.view

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.model.MapTypes

sealed class MapControls {

    object ZoomIn : MapControls()

    data class IgnoredZoomIn(@StringRes val data: Int) : MapControls()

    data class MoveTo(val location: LatLng, val customAnimationDuration: Boolean = false) :
        MapControls()

    data class ChangeType(val mapType: MapTypes) : MapControls()
}