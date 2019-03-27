package com.kksionek.gdzietentramwaj.favorite.view

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView

class SpacesItemDecoration(context: Context, @DimenRes spaceRes: Int) :
    RecyclerView.ItemDecoration() {

    private val space: Int = context.resources.getDimensionPixelOffset(spaceRes)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.left = space
        outRect.right = space
        outRect.bottom = space
        outRect.top = space
    }
}