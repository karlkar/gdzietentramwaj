package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.location.Location
import android.view.ViewGroup

interface AdProviderInterface {
    fun initialize(context: Context)
    fun showAd(adView: ViewGroup)
    fun resume()
    fun pause()
    fun loadAd(context: Context, location: Location)
}
