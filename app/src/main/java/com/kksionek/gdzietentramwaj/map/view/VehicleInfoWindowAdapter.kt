package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.kksionek.gdzietentramwaj.databinding.InfoWindowBinding

class VehicleInfoWindowAdapter(
    context: Context
) : GoogleMap.InfoWindowAdapter {

    private val binding: InfoWindowBinding =
        InfoWindowBinding.inflate(LayoutInflater.from(context), null, false)

    override fun getInfoContents(marker: Marker?): View {
        with(binding) {
            infoWindowTitleTextview.text = marker?.title
            infoWindowDescriptionTextview.text = marker?.snippet
            return root
        }
    }

    override fun getInfoWindow(marker: Marker?): View? = null
}
