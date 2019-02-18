package com.kksionek.gdzietentramwaj.view

import android.content.Context
import android.location.Location
import android.view.ViewGroup

class AdProvider : AdProviderInterface {
    override fun initialize(context: Context, adMobAppId: String) {}

    override fun showAd(adView: ViewGroup) {}

    override fun resume() {}

    override fun pause() {}

    override fun loadAd(context: Context, location: Location) {}
}
