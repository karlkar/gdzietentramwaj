package com.kksionek.gdzietentramwaj.base.view

import android.widget.ImageView
import com.squareup.picasso.Picasso

class PicassoImageLoader(private val picasso: Picasso): ImageLoader {

    override fun load(uri: String, imageView: ImageView) {
        picasso.load(uri).into(imageView)
    }
}