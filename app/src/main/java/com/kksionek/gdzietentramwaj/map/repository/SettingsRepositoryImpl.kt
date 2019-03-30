package com.kksionek.gdzietentramwaj.map.repository

import android.content.Context
import android.preference.PreferenceManager
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository

private const val PREF_FAVORITE_TRAM_VIEW = "FAVORITE_TRAM_VIEW"
private const val PREF_LAST_VERSION = "LAST_VERSION"
private const val PREF_OLD_ICON_SET = "OLD_ICON_SET"
private const val PREF_AUTO_ZOOM = "AUTO_ZOOM"

class SettingsRepositoryImpl(private val context: Context) :
    MapsViewSettingsRepository, IconSettingsManager, VersionRepository, MapSettingsManager {

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

    override fun getPreviouslyLaunchedVersion(): Int = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getInt(PREF_LAST_VERSION, 0)

    override fun saveLastLaunchedVersion(version: Int) {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putInt(PREF_LAST_VERSION, version)
            .apply()
    }

    override fun setIsOldIconSetEnabled(enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PREF_OLD_ICON_SET, enabled)
            .apply()
    }

    override fun isOldIconSetEnabled(): Boolean = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(PREF_OLD_ICON_SET, false)

    override fun setAutoZoomEnabled(enabled: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(PREF_AUTO_ZOOM, enabled)
            .apply()
    }

    override fun isAutoZoomEnabled(): Boolean = PreferenceManager
        .getDefaultSharedPreferences(context)
        .getBoolean(PREF_AUTO_ZOOM, true)
}