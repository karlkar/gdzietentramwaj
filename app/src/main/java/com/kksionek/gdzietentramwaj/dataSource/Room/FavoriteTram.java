package com.kksionek.gdzietentramwaj.dataSource.Room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class FavoriteTram {
    @NonNull @PrimaryKey
    private String mLineId;
    private boolean mFavorite;

    public FavoriteTram(String lineId, boolean favorite) {
        mLineId = lineId;
        mFavorite = favorite;
    }

    public String getLineId() {
        return mLineId;
    }

    public boolean isFavorite() {
        return mFavorite;
    }

    public void setLineId(String lineId) {
        mLineId = lineId;
    }

    public void setFavorite(boolean favorite) {
        mFavorite = favorite;
    }

    @Override
    public String toString() {
        return mLineId;
    }
}
