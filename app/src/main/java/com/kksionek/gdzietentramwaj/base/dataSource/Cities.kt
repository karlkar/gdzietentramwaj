package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.KRAKOW_LATLNG
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.WROCLAW_LATLNG

enum class Cities(val latLng: LatLng, @StringRes val humanReadableName: Int) {
    WARSAW(WARSAW_LATLNG, R.string.warsaw),
    KRAKOW(KRAKOW_LATLNG, R.string.krakow),
    WROCLAW(WROCLAW_LATLNG, R.string.wroclaw);

    companion object {
        fun ofValue(code: Int) = Cities.values().firstOrNull { it.ordinal == code } ?: WARSAW
    }
}
