package com.kksionek.gdzietentramwaj.map.repository

interface MapsViewSettingsRepository: IconSettingsManager {

    fun isFavoriteTramViewEnabled(): Boolean

    fun saveFavoriteTramViewState(enabled: Boolean)

    /**
     * Returns the version number that was previously launched. 0 if it is a first launch
     */
    fun getPreviouslyLaunchedVersion(): Int

    fun saveLastLaunchedVersion(version: Int)
}