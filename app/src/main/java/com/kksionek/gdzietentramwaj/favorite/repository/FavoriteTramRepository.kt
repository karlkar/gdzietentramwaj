package com.kksionek.gdzietentramwaj.favorite.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import io.reactivex.Flowable
import javax.inject.Inject

class FavoriteTramRepository @Inject constructor(
    private val tramDao: TramDao
) {

    fun getAllTrams(city: Cities): Flowable<List<FavoriteTram>> =
        tramDao.getAllFavTrams(city.id)

    fun setTramFavorite(city: Cities, lineId: String, favorite: Boolean) {
        tramDao.setFavorite(city.id, lineId, favorite)
    }
}