package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng

interface MapSettingsManager: MapSettingsProvider {

    fun setAutoZoomEnabled(enabled: Boolean)

    fun setStartLocationEnabled(enabled: Boolean)

    fun setStartLocation(location: LatLng, zoom: Float)
}