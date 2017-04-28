package com.kksionek.gdzietentramwaj.data;

import android.support.annotation.NonNull;
import android.util.Log;

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
        if (isNumber(mLine) && isNumber(((FavoriteTramData) another).getLine()))
            return Integer.valueOf(mLine).compareTo(Integer.valueOf(((FavoriteTramData)another).getLine()));
        else
            return mLine.compareTo(((FavoriteTramData) another).getLine());
    }

    private boolean isNumber(String str) {
        short count = 0;
        char chc[]  = {'0','1','2','3','4','5','6','7','8','9'};
        for (int j = 0; j < str.length(); ++j) {
            for (int i = 0; i < chc.length; i++) {
                if (str.charAt(j) == chc[i]) {
                    count++;
                    break;
                }
            }
            if (count <= j )
                return false;
        }
        return true;
    }
}
