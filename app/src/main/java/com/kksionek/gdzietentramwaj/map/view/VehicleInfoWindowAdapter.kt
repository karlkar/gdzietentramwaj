package com.kksionek.gdzietentramwaj.map.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.kksionek.gdzietentramwaj.R
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.info_window.*

class VehicleInfoWindowAdapter(
    context: Context
) : GoogleMap.InfoWindowAdapter, LayoutContainer {

    private var markerId: String? = null

    @SuppressLint("InflateParams")
    override val containerView: View =
        LayoutInflater.from(context).inflate(R.layout.info_window, null)

    override fun getInfoContents(marker: Marker?): View {
        info_window_title_textview.text = marker?.title
        info_window_description_textview.text = marker?.snippet
        markerId = marker?.tag as String
        return containerView
    }

    override fun getInfoWindow(marker: Marker?): View? {
        return null
    }

}
