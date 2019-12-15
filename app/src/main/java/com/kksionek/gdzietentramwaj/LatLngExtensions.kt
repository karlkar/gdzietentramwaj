package com.kksionek.gdzietentramwaj

import com.google.android.gms.maps.model.LatLng
import com.kksionek.gdzietentramwaj.main.model.GttLocation
import com.kksionek.gdzietentramwaj.main.model.Latitude
import com.kksionek.gdzietentramwaj.main.model.Longitude

fun LatLng.toGTTLocation(): GttLocation = GttLocation(Latitude(latitude), Longitude(longitude))