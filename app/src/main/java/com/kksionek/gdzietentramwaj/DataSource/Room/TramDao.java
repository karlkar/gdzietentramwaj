package com.kksionek.gdzietentramwaj.DataSource.Room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface TramDao {
    @Insert(onConflict = IGNORE)
    void save(TramDao tramDao);

    @Query("SELECT * FROM favoriteTram WHERE mFavorite = true")
    LiveData<FavoriteTram> getFavoriteTrams();
}
