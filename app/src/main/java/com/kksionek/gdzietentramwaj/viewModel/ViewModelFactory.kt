package com.kksionek.gdzietentramwaj.viewModel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.kksionek.gdzietentramwaj.TramApplication
import javax.inject.Inject

class ViewModelFactory @Inject constructor(
    private val application: TramApplication
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(MainActivityViewModel::class.java) ->
                MainActivityViewModel(application) as T
            modelClass.isAssignableFrom(FavoriteLinesActivityViewModel::class.java) ->
                FavoriteLinesActivityViewModel(application) as T
            else ->
                throw IllegalArgumentException("ViewModel Not Found")
        }
    }
}