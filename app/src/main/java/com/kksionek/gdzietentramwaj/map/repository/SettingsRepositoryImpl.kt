package com.kksionek.gdzietentramwaj.map.repository

import android.content.Context
import android.preference.PreferenceManager
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.getDouble
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository
import com.kksionek.gdzietentramwaj.map.dataSource.Cities
import com.kksionek.gdzietentramwaj.putDouble

private const val PREF_FAVORITE_TRAM_VIEW = "FAVORITE_TRAM_VIEW"
private const val PREF_LAST_VERSION = "LAST_VERSION"
private const val PREF_OLD_ICON_SET = "OLD_ICON_SET"
private const val PREF_AUTO_ZOOM = "AUTO_ZOOM"
private const val PREF_START_LOCATION = "START_LOCATION"
private const val PREF_START_LOCATION_LATITUDE = "START_LOCATION_LATITUDE"
private const val PREF_START_LOCATION_LONGITUDE = "START_LOCATION_LONGITTUDE"
private const val PREF_START_LOCATION_ZOOM = "START_LOCATION_ZOOM"

class SettingsRepositoryImpl(context: Context) :
    MapsViewSettingsRepository, IconSettingsManager, VersionRepository, MapSettingsManager {

    private val sharedPreferences = PreferenceManager
        .getDefaultSharedPreferences(context)

    override fun isFavoriteTramViewEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_FAVORITE_TRAM_VIEW, false)

    override fun saveFavoriteTramViewState(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(PREF_FAVORITE_TRAM_VIEW, enabled)
            .apply()
    }

    override fun getPreviouslyLaunchedVersion(): Int =
        sharedPreferences.getInt(PREF_LAST_VERSION, 0)

    override fun saveLastLaunchedVersion(version: Int) {
        sharedPreferences
            .edit()
            .putInt(PREF_LAST_VERSION, version)
            .apply()
    }

    override fun setIsOldIconSetEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(PREF_OLD_ICON_SET, enabled)
            .apply()
    }

    override fun isOldIconSetEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_OLD_ICON_SET, false)

    override fun setAutoZoomEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(PREF_AUTO_ZOOM, enabled)
            .apply()
    }

    override fun isAutoZoomEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_AUTO_ZOOM, true)

    override fun setStartLocationEnabled(enabled: Boolean) {
        sharedPreferences
            .edit()
            .putBoolean(PREF_START_LOCATION, enabled)
            .apply()
    }

    override fun isStartLocationEnabled(): Boolean =
        sharedPreferences.getBoolean(PREF_START_LOCATION, false)

    override fun setStartLocation(location: LatLng, zoom: Float) {
        sharedPreferences
            .edit()
            .putDouble(PREF_START_LOCATION_LATITUDE, location.latitude)
            .putDouble(PREF_START_LOCATION_LONGITUDE, location.longitude)
            .putFloat(PREF_START_LOCATION_ZOOM, zoom)
            .apply()
    }

    override fun getStartLocationPosition(): LatLng? =
        sharedPreferences.run {
            val latitude = getDouble(PREF_START_LOCATION_LATITUDE, 0.0)
            val longitude = getDouble(PREF_START_LOCATION_LONGITUDE, 0.0)
            if (latitude == 0.0 || longitude == 0.0)
                null
            else
                LatLng(latitude, longitude)
        }

    override fun getStartLocationZoom(): Float? =
        sharedPreferences
            .getFloat(PREF_START_LOCATION_ZOOM, 0f)
            .let { if (it == 0f) null else it }

    override fun getCity(): Cities = Cities.WARSAW

    override fun getDefaultZoom(): Float = 15f
}