package com.kksionek.gdzietentramwaj;

public class FavoriteTramData implements Comparable {
    private String mLine;
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
        if (!(o instanceof FavoriteTramData))
            return false;
        return mLine.equals(((FavoriteTramData)o).getLine());
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
    public int compareTo(Object another) {
        if (!(another instanceof FavoriteTramData))
            return -1;
        return new Integer(mLine).compareTo(new Integer(((FavoriteTramData)another).getLine()));
    }
}
