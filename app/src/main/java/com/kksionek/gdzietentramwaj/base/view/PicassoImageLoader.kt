package com.kksionek.gdzietentramwaj.base.view

import android.widget.ImageView
import com.squareup.picasso.Picasso

class PicassoImageLoader: ImageLoader {

    override fun load(uri: String, imageView: ImageView) {
        Picasso.get().load(uri).into(imageView)
    }
}