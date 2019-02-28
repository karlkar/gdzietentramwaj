package com.kksionek.gdzietentramwaj.map.view

import com.google.android.gms.maps.model.LatLng

sealed class MapControls {

    object ZoomIn : MapControls()

    data class MoveTo(val location: LatLng) : MapControls()
}