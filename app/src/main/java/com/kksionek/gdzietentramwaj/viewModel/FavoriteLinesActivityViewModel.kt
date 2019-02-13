package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel

import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.repository.TramRepository

class FavoriteLinesActivityViewModel(
    application: TramApplication
) : ViewModel() {

    private val mTramRepository: TramRepository = application.appComponent.tramRepository

    fun getFavoriteTrams(): LiveData<List<FavoriteTram>> =
        mTramRepository.allFavTrams

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        mTramRepository.setTramFavorite(lineId, favorite)
    }
}
