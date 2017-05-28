package com.kksionek.gdzietentramwaj.DataSource.Room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity
public class FavoriteTram {
    @PrimaryKey
    private String mLineId;
    private boolean mFavorite;

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
}
