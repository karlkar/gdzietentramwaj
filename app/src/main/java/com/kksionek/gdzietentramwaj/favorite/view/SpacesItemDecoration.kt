package com.kksionek.gdzietentramwaj.favorite.view

import android.content.Context
import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View

class SpacesItemDecoration(context: Context, @DimenRes spaceRes: Int) :
    RecyclerView.ItemDecoration() {

    private val space: Int = context.resources.getDimensionPixelOffset(spaceRes)

    override fun getItemOffsets(
        outRect: Rect, view: View,
        parent: RecyclerView, state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        outRect.top = space
    }
}