package com.kksionek.gdzietentramwaj

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun LatLng.toLocation(): Location = Location("").also {
    it.longitude = longitude
    it.latitude = latitude
}