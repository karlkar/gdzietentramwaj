package com.kksionek.gdzietentramwaj.favorite.repository

import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import io.reactivex.Flowable
import javax.inject.Inject

class FavoriteTramRepository @Inject constructor(
    private val tramDao: TramDao
) {

    val allFavTrams: Flowable<List<FavoriteTram>>
        get() = tramDao.getAllFavTrams()

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        tramDao.setFavorite(lineId, favorite)
    }
}