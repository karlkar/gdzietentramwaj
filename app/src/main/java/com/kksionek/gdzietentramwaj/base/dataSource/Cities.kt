package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.annotation.StringRes
import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.BIELSKO_LATLNG
import com.kksionek.gdzietentramwaj.GOP_LATLNG
import com.kksionek.gdzietentramwaj.KRAKOW_LATLNG
import com.kksionek.gdzietentramwaj.LODZ_LATLNG
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.SZCZECIN_LATLNG
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG
import com.kksionek.gdzietentramwaj.WROCLAW_LATLNG
import com.kksionek.gdzietentramwaj.ZIELONA_LATLNG

enum class Cities(val id: Int, val latLng: LatLng, @StringRes val humanReadableName: Int) {
    WARSAW(1, WARSAW_LATLNG, R.string.warsaw),
    KRAKOW(2, KRAKOW_LATLNG, R.string.krakow),
    WROCLAW(3, WROCLAW_LATLNG, R.string.wroclaw),
    LODZ(4, LODZ_LATLNG, R.string.lodz),
    SZCZECIN(5, SZCZECIN_LATLNG, R.string.szczecin),
    BIELSKO(6, BIELSKO_LATLNG, R.string.bielsko),
    ZIELONA(7, ZIELONA_LATLNG, R.string.zielona_gora),
    GOP(8, GOP_LATLNG, R.string.gop);

    // `id` is a field that has to be stable as it is used in favorite trams saving

    companion object {
        fun ofValue(code: Int) = values().firstOrNull { it.id == code } ?: WARSAW
    }
}
