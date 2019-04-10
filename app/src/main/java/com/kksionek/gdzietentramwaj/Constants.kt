package com.kksionek.gdzietentramwaj

import android.location.Location
import com.google.android.gms.maps.model.LatLng

const val WARSAW_LAT = 52.231841
const val WARSAW_LNG = 21.005940

const val KRAKOW_LAT = 50.059614
const val KRAKOW_LNG = 19.942137

val WARSAW_LOCATION = Location("").apply {
    latitude = WARSAW_LAT
    longitude = WARSAW_LNG
}

val KRAKOW_LOCATION = Location("").apply {
    latitude = KRAKOW_LAT
    longitude = KRAKOW_LNG
}

val WARSAW_LATLNG = LatLng(WARSAW_LAT, WARSAW_LNG)
val KRAKOW_LATLNG = LatLng(KRAKOW_LAT, KRAKOW_LNG)