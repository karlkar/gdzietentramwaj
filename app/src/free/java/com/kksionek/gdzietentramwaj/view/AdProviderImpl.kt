package com.kksionek.gdzietentramwaj.view

import android.content.Context
import android.location.Location
import android.view.View.VISIBLE
import android.view.ViewGroup

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.view.AdProvider

class AdProviderImpl(applicationContext: Context) : AdProvider {

    private var adView: AdView? = null

    init {
        if (BuildConfig.DEBUG) {
            val requestConfiguration = MobileAds.getRequestConfiguration().toBuilder()
                .setTestDeviceIds(
                    listOf(
                        applicationContext.getString(R.string.adMobTestDeviceS7Edge),
                        applicationContext.getString(R.string.adMobTestDeviceS7Edge2)
                    )
                ).build()
            MobileAds.setRequestConfiguration(requestConfiguration)
        }
        MobileAds.initialize(applicationContext)
    }

    override fun showAd(adView: ViewGroup) {
        this.adView = adView as AdView
    }

    override fun resume() {
        adView?.resume()
    }

    override fun pause() {
        adView?.pause()
    }

    override fun loadAd(location: Location) {
        adView?.let {
            it.visibility = VISIBLE
            val adRequest = AdRequest.Builder()
                .setLocation(location)
                .build()
            it.loadAd(adRequest)
        }
    }
}
