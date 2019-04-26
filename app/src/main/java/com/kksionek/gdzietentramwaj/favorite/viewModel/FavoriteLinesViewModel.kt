package com.kksionek.gdzietentramwaj.favorite.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.favorite.repository.FavoriteTramRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.map.view.UiState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FavoriteLinesViewModel @Inject constructor(
    private val favoriteTramRepository: FavoriteTramRepository,
    private val crashReportingService: CrashReportingService,
    mapSettingsProvider: MapSettingsProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var _favoriteTrams = MutableLiveData<UiState<List<FavoriteTram>>>()
    val favoriteTrams: LiveData<UiState<List<FavoriteTram>>> = _favoriteTrams

    private val selectedCity: Cities = mapSettingsProvider.getCity()

    init {
        forceReloadFavorites()
    }

    fun forceReloadFavorites() {
        compositeDisposable.clear()
        compositeDisposable.add(favoriteTramRepository.getAllTrams(selectedCity)
            .subscribeOn(Schedulers.io())
            .map { UiState.Success(it) as UiState<List<FavoriteTram>> }
            .onErrorReturn {
                Log.e(TAG, "Failed getting all the favorites from the database", it)
                crashReportingService.reportCrash(
                    it,
                    "Failed getting all the favorites from the database"
                )
                UiState.Error(R.string.favorites_failed_to_load)
            }
            .startWith(UiState.InProgress())
            .subscribe { list -> _favoriteTrams.postValue(list) }
        )
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        compositeDisposable.add(
            favoriteTramRepository.setTramFavorite(selectedCity, lineId, favorite)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        Log.v(TAG, "Tram saved as favorite")
                    },
                    {
                        Log.e(TAG, "Failed to save the tram as favorite", it)
                        crashReportingService.reportCrash(
                            it,
                            "Failed to save the tram as favorite"
                        )
                    })
        )
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    companion object {
        const val TAG = "FavoriteLinesActivityVi"
    }
}
