package com.kksionek.gdzietentramwaj.base.di

import android.arch.lifecycle.ViewModelProvider
import com.kksionek.gdzietentramwaj.base.viewModel.ViewModelFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    abstract fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory
}