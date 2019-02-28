package com.kksionek.gdzietentramwaj.favorite.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

class GridViewItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
) : RelativeLayout(context, attrs, defAttrStyle) {

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}