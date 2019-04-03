package com.kksionek.gdzietentramwaj.map.repository

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.map.dataSource.Cities

interface MapSettingsProvider {

    fun isAutoZoomEnabled(): Boolean

    fun isStartLocationEnabled(): Boolean

    fun getStartLocationPosition(): LatLng?

    fun getStartLocationZoom(): Float?

    fun getCity(): Cities

    fun getDefaultZoom(): Float
}