package com.kksionek.gdzietentramwaj.dataSource.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.IGNORE
import android.arch.persistence.room.Query

@Dao
interface TramDao {

    @Query("SELECT * FROM favoriteTram")
    fun getAllFavTrams(): LiveData<List<FavoriteTram>>

    @Query("SELECT lineId FROM favoriteTram WHERE isFavorite = 1")
    fun getFavoriteTrams(): LiveData<List<String>>

    @Insert(onConflict = IGNORE)
    fun save(tramData: FavoriteTram)

    @Insert(onConflict = IGNORE)
    fun save(tramDataList: List<FavoriteTram>)

    @Query("UPDATE favoriteTram SET isFavorite = :favorite WHERE lineId = :lineId")
    fun setFavorite(lineId: String, favorite: Boolean)
}
