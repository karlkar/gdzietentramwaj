package com.kksionek.gdzietentramwaj;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class GridViewItem extends RelativeLayout {

    public GridViewItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}