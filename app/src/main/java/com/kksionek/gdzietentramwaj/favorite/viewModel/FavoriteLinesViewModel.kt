package com.kksionek.gdzietentramwaj.favorite.viewModel

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.R
import com.kksionek.gdzietentramwaj.addToDisposable
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.favorite.repository.FavoriteVehiclesRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.map.view.UiState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class FavoriteLinesViewModel @ViewModelInject constructor(
    private val favoriteVehiclesRepository: FavoriteVehiclesRepository,
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
        favoriteVehiclesRepository.getAllVehicles(selectedCity)
            .subscribeOn(Schedulers.io())
            .map { UiState.Success(it) as UiState<List<FavoriteTram>> }
            .onErrorReturn {
                Timber.e(it, "Failed getting all the favorites from the database")
                crashReportingService.reportCrash(
                    it,
                    "Failed getting all the favorites from the database"
                )
                UiState.Error(R.string.favorites_failed_to_load)
            }
            .startWith(UiState.InProgress())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { list -> _favoriteTrams.setValue(list) }
            .addToDisposable(compositeDisposable)
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        favoriteVehiclesRepository.setVehicleFavorite(selectedCity, lineId, favorite)
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    Timber.v("Tram saved as favorite")
                },
                {
                    Timber.e(it, "Failed to save the tram as favorite")
                    crashReportingService.reportCrash(
                        it,
                        "Failed to save the tram as favorite"
                    )
                })
            .addToDisposable(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}
