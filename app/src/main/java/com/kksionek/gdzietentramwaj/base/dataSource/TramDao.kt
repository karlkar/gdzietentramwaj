package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface TramDao {

    @Query("SELECT * FROM favoriteTram")
    fun getAllFavTrams(): Flowable<List<FavoriteTram>>

    @Query("SELECT lineId FROM favoriteTram WHERE isFavorite = 1")
    fun getFavoriteTrams(): Flowable<List<String>>

    @Insert(onConflict = IGNORE)
    fun save(tramData: FavoriteTram)

    @Insert(onConflict = IGNORE)
    fun save(tramDataList: List<FavoriteTram>)

    @Query("UPDATE favoriteTram SET isFavorite = :favorite WHERE lineId = :lineId")
    fun setFavorite(lineId: String, favorite: Boolean)
}
