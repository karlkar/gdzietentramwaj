package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng

interface MapSettingsProvider {

    fun isAutoZoomEnabled(): Boolean

    fun isStartLocationEnabled(): Boolean

    fun getStartLocationPosition(): LatLng?

    fun getStartLocationZoom(): Float?
}