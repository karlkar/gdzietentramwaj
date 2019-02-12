package com.kksionek.gdzietentramwaj.repository

import android.arch.lifecycle.LiveData

import com.kksionek.gdzietentramwaj.dataSource.TramDataWrapper
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao

import javax.inject.Inject

class TramRepository @Inject constructor(
    private val mTramDao: TramDao,
    tramInterface: TramInterface
) {
    private val mFavoriteRepositoryAdder: FavoriteLinesConsumer = FavoriteLinesConsumer(mTramDao)

    private val _tramLiveData: TramLiveData = TramLiveData(tramInterface, mFavoriteRepositoryAdder)
    val dataStream: LiveData<TramDataWrapper>
        get() = _tramLiveData

    val allFavTrams: LiveData<List<FavoriteTram>>
        get() = mTramDao.getAllFavTrams()

    val favoriteTrams: LiveData<List<String>>
        get() = mTramDao.getFavoriteTrams()

    fun forceReload() {
        _tramLiveData.forceReload()
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        mTramDao.setFavorite(lineId, favorite)
    }
}
