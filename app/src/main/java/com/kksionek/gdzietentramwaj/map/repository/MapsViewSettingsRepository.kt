package com.kksionek.gdzietentramwaj.map.repository

interface MapsViewSettingsRepository {

    fun isFavoriteViewEnabled(): Boolean

    fun saveFavoriteViewState(enabled: Boolean)
}