package com.kksionek.gdzietentramwaj.base.view

import android.widget.ImageView

interface ImageLoader {
    fun load(uri: String, imageView: ImageView)
}