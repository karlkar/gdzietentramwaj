package com.kksionek.gdzietentramwaj.settings.di

import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent

@InstallIn(FragmentComponent::class)
@Module
class SettingsFragmentModule {

    @Provides
    internal fun provideIconSettingsManager(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): IconSettingsManager = settingsRepositoryImpl

    @Provides
    internal fun provideMapSettingsManager(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): MapSettingsManager = settingsRepositoryImpl
}