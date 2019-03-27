package com.kksionek.gdzietentramwaj.map.repository

interface MapsViewSettingsRepository {

    fun isFavoriteTramViewEnabled(): Boolean

    fun saveFavoriteTramViewState(enabled: Boolean)
}