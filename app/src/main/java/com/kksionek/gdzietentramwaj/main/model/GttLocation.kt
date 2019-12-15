package com.kksionek.gdzietentramwaj.main.model

import android.location.Location
import android.location.LocationManager
import com.google.android.gms.maps.model.LatLng

data class GttLocation(val latitude: Latitude, val longitude: Longitude) // TODO: Why not LatLng?

inline class Latitude(val value: Double)
inline class Longitude(val value: Double)

fun GttLocation.toLocation() = Location(LocationManager.NETWORK_PROVIDER).also {
    it.latitude = latitude.value
    it.longitude = longitude.value
}

fun GttLocation.toLatLng() = LatLng(latitude.value, longitude.value)