package com.kksionek.gdzietentramwaj.map.repository

interface IconSettingsManager: IconSettingsProvider {

    fun setIsOldIconSetEnabled(enabled: Boolean)
}