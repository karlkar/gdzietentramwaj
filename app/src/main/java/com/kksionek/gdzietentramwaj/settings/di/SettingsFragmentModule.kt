package com.kksionek.gdzietentramwaj.settings.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.settings.viewModel.SettingsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module(includes = [SettingsFragmentModule.SettingsViewModelModule::class])
class SettingsFragmentModule {

    @Module
    interface SettingsViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(SettingsViewModel::class)
        fun bindSettingsViewModel(settingsViewModel: SettingsViewModel): ViewModel
    }

    @Singleton
    @Provides
    internal fun provideIconSettingsManager(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): IconSettingsManager = settingsRepositoryImpl
}