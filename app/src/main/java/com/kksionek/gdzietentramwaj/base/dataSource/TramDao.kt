package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface TramDao {

    @Query("SELECT * FROM favoriteTram WHERE cityId = :cityId")
    fun getAllFavTrams(cityId: Int): Flowable<List<FavoriteTram>>

    @Query("SELECT lineId FROM favoriteTram WHERE isFavorite = 1 AND cityId = :cityId")
    fun getFavoriteTrams(cityId: Int): Flowable<List<String>>

    @Insert(onConflict = IGNORE)
    fun save(tramData: FavoriteTram)

    @Insert(onConflict = IGNORE)
    fun save(tramDataList: List<FavoriteTram>)

    @Query("UPDATE favoriteTram SET isFavorite = :favorite WHERE lineId = :lineId AND cityId = :cityId")
    fun setFavorite(cityId: Int, lineId: String, favorite: Boolean)
}
