package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.kksionek.gdzietentramwaj.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.repository.TramRepository
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FavoriteLinesActivityViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val crashReportingService: CrashReportingService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private var _favoriteTrams =
        MutableLiveData<List<FavoriteTram>>().apply { postValue(emptyList()) }
    val favoriteTrams: LiveData<List<FavoriteTram>> = _favoriteTrams

    init {
        compositeDisposable.add(tramRepository.allFavTrams
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
            Completable.fromCallable { tramRepository.setTramFavorite(lineId, favorite) }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Log.v(TAG, "Tram saved as favorite")
                }, {
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
