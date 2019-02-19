package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.kksionek.gdzietentramwaj.CrashReportingService
import com.kksionek.gdzietentramwaj.dataSource.room.FavoriteTram
import com.kksionek.gdzietentramwaj.repository.TramRepository
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FavoriteLinesActivityViewModel @Inject constructor(
    private val tramRepository: TramRepository,
    private val crashReportingService: CrashReportingService
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    fun getFavoriteTrams(): LiveData<List<FavoriteTram>> =
        tramRepository.allFavTrams

    fun setTramFavorite(lineId: String, favorite: Boolean) {
        compositeDisposable.add(
            Completable.fromCallable { tramRepository.setTramFavorite(lineId, favorite) }
                .subscribeOn(Schedulers.io())
                .subscribe({}, {
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
