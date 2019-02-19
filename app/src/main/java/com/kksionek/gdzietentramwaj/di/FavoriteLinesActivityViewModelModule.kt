package com.kksionek.gdzietentramwaj.di

import android.arch.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.viewModel.FavoriteLinesActivityViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FavoriteLinesActivityViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteLinesActivityViewModel::class)
    abstract fun bindFavoriteLinesActivityViewModel(favoriteLinesActivityViewModel: FavoriteLinesActivityViewModel): ViewModel
}