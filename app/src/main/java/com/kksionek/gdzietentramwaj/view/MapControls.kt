package com.kksionek.gdzietentramwaj.view

import com.google.android.gms.maps.model.LatLng

sealed class MapControls {

    object ZoomIn : MapControls()

    data class MoveTo(val location: LatLng) : MapControls()
}