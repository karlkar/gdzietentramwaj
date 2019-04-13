package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.KRAKOW_LATLNG
import com.kksionek.gdzietentramwaj.LODZ_LATLNG
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.SZCZECIN_LATLNG
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.WROCLAW_LATLNG

enum class Cities(val latLng: LatLng, @StringRes val humanReadableName: Int) {
    WARSAW(WARSAW_LATLNG, R.string.warsaw),
    KRAKOW(KRAKOW_LATLNG, R.string.krakow),
    WROCLAW(WROCLAW_LATLNG, R.string.wroclaw),
    LODZ(LODZ_LATLNG, R.string.lodz),
    SZCZECIN(SZCZECIN_LATLNG, R.string.szczecin);

    companion object {
        fun ofValue(code: Int) = values().firstOrNull { it.ordinal == code } ?: WARSAW
    }
}
