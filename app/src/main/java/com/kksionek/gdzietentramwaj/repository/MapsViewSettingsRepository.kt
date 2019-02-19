package com.kksionek.gdzietentramwaj.repository

interface MapsViewSettingsRepository {

    fun isFavoriteTramViewEnabled(): Boolean

    fun saveFavoriteTramViewState(enabled: Boolean)
}