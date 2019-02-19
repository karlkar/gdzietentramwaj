package com.kksionek.gdzietentramwaj.repository

import android.content.Context
import android.preference.PreferenceManager

const val PREF_FAVORITE_TRAM_VIEW = "FAVORITE_TRAM_VIEW"

class MapsViewSettingsRepositoryImpl(private val context: Context) :
    MapsViewSettingsRepository {

    override fun isFavoriteTramViewEnabled(): Boolean = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(PREF_FAVORITE_TRAM_VIEW, false)

    override fun saveFavoriteTramViewState(enabled: Boolean) {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PREF_FAVORITE_TRAM_VIEW, enabled)
            .apply()
    }
}