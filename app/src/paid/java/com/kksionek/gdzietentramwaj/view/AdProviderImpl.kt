package com.kksionek.gdzietentramwaj.view

import android.content.Context
import android.location.Location
import android.view.ViewGroup
import com.kksionek.gdzietentramwaj.map.view.AdProvider

class AdProviderImpl(application: Context) : AdProvider {
    override fun showAd(adView: ViewGroup) {}

    override fun resume() {}

    override fun pause() {}

    override fun loadAd(location: Location) {}
}
