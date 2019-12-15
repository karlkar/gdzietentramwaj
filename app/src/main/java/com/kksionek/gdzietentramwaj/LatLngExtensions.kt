package com.kksionek.gdzietentramwaj

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng

fun LatLng.toLocation(): Location = Location(LocationManager.NETWORK_PROVIDER).also {
    it.latitude = latitude
    it.longitude = longitude
}