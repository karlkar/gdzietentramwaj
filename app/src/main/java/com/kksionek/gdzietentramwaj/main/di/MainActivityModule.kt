package com.kksionek.gdzietentramwaj.main.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module(includes = [MainActivityModule.MainViewModelModule::class])
class MainActivityModule {

    @Module
    interface MainViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(MainViewModel::class)
        fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel
    }

    @Singleton
    @Provides
    fun providesVersionRepository(settingsRepositoryImpl: SettingsRepositoryImpl): VersionRepository =
        settingsRepositoryImpl
}