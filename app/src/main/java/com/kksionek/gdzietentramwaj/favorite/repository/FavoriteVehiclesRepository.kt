package com.kksionek.gdzietentramwaj.favorite.repository

import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import io.reactivex.Completable
import io.reactivex.Flowable
import javax.inject.Inject

class FavoriteVehiclesRepository @Inject constructor(
    private val tramDao: TramDao
) {

    fun getAllVehicles(city: Cities): Flowable<List<FavoriteTram>> =
        tramDao.getAllVehicles(city.id).distinctUntilChanged()

    fun setTramFavorite(city: Cities, lineId: String, favorite: Boolean): Completable =
        Completable.fromAction { tramDao.setFavorite(city.id, lineId, favorite) }
}