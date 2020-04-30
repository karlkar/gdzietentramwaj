package com.kksionek.gdzietentramwaj.view

import android.content.Context
import android.location.Location
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface

class AdProvider : AdProviderInterface {
    override fun initialize(context: Context) {}

    override fun showAd(adView: ViewGroup) {}

    override fun resume() {}

    override fun pause() {}

    override fun loadAd(context: Context, location: Location) {}
}
