package com.kksionek.gdzietentramwaj.base.dataSource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import io.reactivex.Flowable

@Dao
interface TramDao {

    @Query("SELECT * FROM favoriteTram WHERE cityId = :cityId")
    fun getAllVehicles(cityId: Int): Flowable<List<FavoriteTram>>

    @Insert(onConflict = IGNORE)
    fun save(tramData: FavoriteTram)

    @Insert(onConflict = IGNORE)
    fun save(vehicleDataList: List<FavoriteTram>)

    @Query("UPDATE favoriteTram SET isFavorite = :favorite WHERE lineId = :lineId AND cityId = :cityId")
    fun setFavorite(cityId: Int, lineId: String, favorite: Boolean)
}
