package com.kksionek.gdzietentramwaj.map.view

import android.location.Location
import android.view.ViewGroup

interface AdProvider {
    fun showAd(adView: ViewGroup)
    fun resume()
    fun pause()
    fun loadAd(location: Location)
}
