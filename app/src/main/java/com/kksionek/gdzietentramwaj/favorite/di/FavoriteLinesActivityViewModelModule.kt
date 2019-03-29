package com.kksionek.gdzietentramwaj.favorite.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.favorite.viewModel.FavoriteLinesViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class FavoriteLinesActivityViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(FavoriteLinesViewModel::class)
    abstract fun bindFavoriteLinesActivityViewModel(favoriteLinesViewModel: FavoriteLinesViewModel): ViewModel
}