package com.kksionek.gdzietentramwaj.map.dataSource

import com.google.android.gms.maps.GoogleMap

enum class MapTypes(val googleCode: Int) {
    NORMAL(GoogleMap.MAP_TYPE_NORMAL),
    SATELLITE(GoogleMap.MAP_TYPE_SATELLITE),
    TERRAIN(GoogleMap.MAP_TYPE_TERRAIN),
    HYBRID(GoogleMap.MAP_TYPE_HYBRID);

    fun next(): MapTypes = values()[(values().indexOf(this) + 1) % values().size]
    companion object {
        fun ofValue(code: Int) = values().firstOrNull { it.googleCode == code } ?: NORMAL
    }
}