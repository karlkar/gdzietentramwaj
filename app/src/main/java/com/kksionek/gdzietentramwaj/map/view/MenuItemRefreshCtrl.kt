package com.kksionek.gdzietentramwaj.map.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView

import com.kksionek.gdzietentramwaj.R

@SuppressLint("InflateParams")
class MenuItemRefreshCtrl(
    private val context: Context,
    private val menuItem: MenuItem
) {
    private val rotationAnimation: Animation by lazy {
        AnimationUtils.loadAnimation(context, R.anim.anim_rotate)
    }
    private val refreshImage: ImageView by lazy {
        LayoutInflater.from(context).inflate(R.layout.view_refresh_action, null) as ImageView
    }

    fun startAnimation() {
        if (menuItem.actionView == null) {
            refreshImage.startAnimation(rotationAnimation)
            menuItem.actionView = refreshImage
        }
        menuItem.isEnabled = false
    }

    fun endAnimation() {
        if (menuItem.actionView != null) {
            menuItem.actionView.clearAnimation()
            menuItem.actionView = null
        }
        menuItem.isEnabled = true
    }
}
