package com.kksionek.gdzietentramwaj;

import android.content.Context;

import java.util.Set;

public class FavoriteManager {

    private final Set<String> mFavoriteTramData;
    private boolean mChanged = false;

    public FavoriteManager(Context ctx) {
        PrefManager.init(ctx);
        mFavoriteTramData = PrefManager.getFavoriteTramData();
    }

    public boolean isFavorite(String line) {
        return mFavoriteTramData.contains(line);
    }

    public void setFavorite(String line, boolean favorite) {
        if (favorite)
            mFavoriteTramData.add(line);
        else
            mFavoriteTramData.remove(line);
        PrefManager.setFavoriteTramData(mFavoriteTramData);
    }

    public Set<String> getFavoriteTramData() {
        return mFavoriteTramData;
    }

    public void markChanged() {
        mChanged = true;
    }

    public boolean checkIfChangedAndReset() {
        return mChanged;
    }
}