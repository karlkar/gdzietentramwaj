package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.MapTypes

interface MapSettingsManager: MapSettingsProvider {

    fun setAutoZoomEnabled(enabled: Boolean)

    fun setStartLocationEnabled(enabled: Boolean)

    fun setStartLocation(location: LatLng, zoom: Float)

    fun setBrigadeShowingEnabled(enabled: Boolean)

    fun setMapType(type: MapTypes)
}