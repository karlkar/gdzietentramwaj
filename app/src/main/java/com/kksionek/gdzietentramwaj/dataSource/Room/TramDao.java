package com.kksionek.gdzietentramwaj.dataSource.Room;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import static android.arch.persistence.room.OnConflictStrategy.IGNORE;

@Dao
public interface TramDao {
//    @Query("INSERT OR IGNORE INTO favoriteTram(mLineId, mFavorite) VALUES (:lineId, :favorite)")
//    void save(String lineId, boolean favorite);

    @Insert(onConflict = IGNORE)
    void save(FavoriteTram tramData);

    @Insert(onConflict = IGNORE)
    void save(List<FavoriteTram> tramDataList);

    @Query("SELECT * FROM favoriteTram")
    LiveData<List<FavoriteTram>> getAllFavTrams();

    @Query("UPDATE favoriteTram SET mFavorite = :favorite WHERE mLineId = :lineId")
    void setFavorite(String lineId, boolean favorite);

    @Query("SELECT mLineId FROM favoriteTram WHERE mFavorite")
    LiveData<List<String>> getFavoriteTrams();
}
