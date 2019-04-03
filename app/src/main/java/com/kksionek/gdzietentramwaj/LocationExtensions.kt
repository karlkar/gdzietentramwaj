package com.kksionek.gdzietentramwaj

import android.location.Location
import com.google.android.gms.maps.model.LatLng

fun Location.toLatLng() = LatLng(latitude, longitude)