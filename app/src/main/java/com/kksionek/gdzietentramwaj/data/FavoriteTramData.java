package com.kksionek.gdzietentramwaj.data;

import android.support.annotation.NonNull;

public class FavoriteTramData implements Comparable {
    private final String mLine;
    private boolean mFavorite;

    public FavoriteTramData(String line, boolean favorite) {
        mLine = line;
        mFavorite = favorite;
    }

    @Override
    public int hashCode() {
        return mLine.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FavoriteTramData && mLine.equals(((FavoriteTramData) o).getLine());
    }

    public String getLine() {
        return mLine;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        if (!(another instanceof FavoriteTramData))
            return -1;
        return Integer.valueOf(mLine).compareTo(Integer.valueOf(((FavoriteTramData)another).getLine()));
    }
}
