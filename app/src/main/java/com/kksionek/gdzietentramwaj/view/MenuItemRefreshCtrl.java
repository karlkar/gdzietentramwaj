package com.kksionek.gdzietentramwaj.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.kksionek.gdzietentramwaj.R;

public class MenuItemRefreshCtrl {
    private final Context mCtx;
    private final MenuItem mMenuItem;
    private Animation mRotationAnimation = null;
    private ImageView mRefreshImage = null;

    public MenuItemRefreshCtrl(@NonNull Context ctx, @NonNull MenuItem menuItem) {
        mCtx = ctx;
        mMenuItem = menuItem;
    }

    public void startAnimation() {
        if (mRefreshImage == null) {
            LayoutInflater inflater = (LayoutInflater) mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mRefreshImage = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);
        }
        if (mRotationAnimation == null)
            mRotationAnimation = AnimationUtils.loadAnimation(mCtx, R.anim.anim_rotate);
        if (mMenuItem.getActionView() == null) {
            mRefreshImage.startAnimation(mRotationAnimation);
            mMenuItem.setActionView(mRefreshImage);
            mMenuItem.setEnabled(false);
        }
    }

    public void endAnimation() {
        if (mMenuItem.getActionView() != null) {
            mMenuItem.getActionView().clearAnimation();
            mMenuItem.setActionView(null);
        }
        mMenuItem.setEnabled(true);
    }
}
