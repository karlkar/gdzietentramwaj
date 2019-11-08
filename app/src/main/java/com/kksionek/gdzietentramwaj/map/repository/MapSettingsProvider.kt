package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.model.MapTypes

interface MapSettingsProvider {

    fun isAutoZoomEnabled(): Boolean

    fun isStartLocationEnabled(): Boolean

    fun getStartLocationPosition(): LatLng?

    fun getStartLocationZoom(): Float?

    fun isBrigadeShowingEnabled(): Boolean

    fun isTrafficShowingEnabled(): Boolean

    fun getCity(): Cities

    fun getDefaultZoom(): Float

    fun getMapType(): MapTypes
}