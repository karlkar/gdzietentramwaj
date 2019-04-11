package com.kksionek.gdzietentramwaj.base.dataSource

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.KRAKOW_LATLNG
import com.kksionek.gdzietentramwaj.WARSAW_LATLNG

enum class Cities(val latLng: LatLng) {
    WARSAW(WARSAW_LATLNG),
    KRAKOW(KRAKOW_LATLNG);

    companion object {
        fun ofValue(code: Int) = Cities.values().firstOrNull { it.ordinal == code } ?: WARSAW
    }
}
