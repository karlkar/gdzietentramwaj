package com.kksionek.gdzietentramwaj.map.view

import android.content.Context
import android.view.View
import android.view.animation.BounceInterpolator
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.map.model.FollowedTramData
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.fragment_map.*

class FollowedView(
    private val context: Context,
    override val containerView: View,
    mapsViewModel: MapsViewModel
) : LayoutContainer {

    init {
        val followedTramData = mapsViewModel.followedVehicle
        if (followedTramData == null) {
            hideFollowedView(animate = false)
        } else {
            showFollowedView(followedTramData)
        }

        map_followed_cancel_button.setOnClickListener {
            mapsViewModel.followedVehicle = null
            hideFollowedView()
        }
    }

    fun showFollowedView(marker: FollowedTramData) {
        map_followed_constraintlayout.animate()
            .y(0f)
            .setDuration(1000L)
            .setInterpolator(BounceInterpolator())
            .start()
        map_followed_textview.text =
            context.getString(R.string.map_followed_text, marker.title, marker.snippet)
    }

    fun hideFollowedView(animate: Boolean = true) {
        if (animate) {
            map_followed_constraintlayout.animate()
                .y(-map_followed_constraintlayout.height.toFloat())
                .setInterpolator(BounceInterpolator())
                .setDuration(1000L)
                .start()
        } else {
            map_followed_constraintlayout.apply {
                post {
                    y = -height.toFloat()
                    visibility = View.VISIBLE
                }
            }
        }
    }
}