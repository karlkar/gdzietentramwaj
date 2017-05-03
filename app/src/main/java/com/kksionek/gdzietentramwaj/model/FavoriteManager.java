package com.kksionek.gdzietentramwaj.model;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Set;

public class FavoriteManager {

    private final Set<String> mFavoriteTramData;

    public FavoriteManager(@NonNull Context ctx) {
        PrefManager.init(ctx);
        mFavoriteTramData = PrefManager.getFavoriteTramData();
    }

    public boolean isFavorite(@NonNull String line) {
        return mFavoriteTramData.contains(line);
    }

    public void setFavorite(@NonNull String line, boolean favorite) {
        if (favorite)
            mFavoriteTramData.add(line);
        else
            mFavoriteTramData.remove(line);
        PrefManager.setFavoriteTramData(mFavoriteTramData);
    }
}