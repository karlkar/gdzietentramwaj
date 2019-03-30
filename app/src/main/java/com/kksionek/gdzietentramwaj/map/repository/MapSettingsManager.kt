package com.kksionek.gdzietentramwaj.map.repository

interface MapSettingsManager: MapSettingsProvider {

    fun setAutoZoomEnabled(enabled: Boolean)
}