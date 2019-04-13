package com.kksionek.gdzietentramwaj.favorite.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.base.dataSource.FavoriteTram
import com.kksionek.gdzietentramwaj.favorite.repository.FavoriteTramRepository
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FavoriteLinesViewModel @Inject constructor(
    private val favoriteTramRepository: FavoriteTramRepository,
    private val crashReportingService: CrashReportingService,
    mapSettingsProvider: MapSettingsProvider
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var _favoriteTrams =
        MutableLiveData<List<FavoriteTram>>().apply { postValue(emptyList()) }
    val favoriteTrams: LiveData<List<FavoriteTram>> = _favoriteTrams

    private val selectedCity: Cities = mapSettingsProvider.getCity()

    init {
        compositeDisposable.add(favoriteTramRepository.getAllTrams(selectedCity)
            .subscribeOn(Schedulers.io())
            .onErrorResumeNext { throwable: Throwable ->
                Log.e(TAG, "Failed getting all the favorites from the database", throwable)
                crashReportingService.reportCrash(
                    throwable,
                    "Failed getting all the favorites from the database"
                )
                Flowable.empty()
            }
            .subscribe { list -> _favoriteTrams.postValue(list) }
        )
    }

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        compositeDisposable.add(
            Completable.fromCallable {
                favoriteTramRepository.setTramFavorite(selectedCity, lineId, favorite)
            }
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
