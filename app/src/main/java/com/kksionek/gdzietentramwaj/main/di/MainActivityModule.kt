package com.kksionek.gdzietentramwaj.main.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ActivityScope
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepositoryImpl
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityCheckerImpl
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.repository.LocationRepositoryImpl
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface
import com.kksionek.gdzietentramwaj.view.AdProvider
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

    @ActivityScope
    @Provides
    internal fun provideAdProvider(): AdProviderInterface = AdProvider()

    @Singleton
    @Provides
    fun provideAppUpdateRepository(context: Context): AppUpdateRepository =
        AppUpdateRepositoryImpl(context)

    @Singleton
    @Provides
    fun provideGoogleApiAvailabilityChecker(context: Context): GoogleApiAvailabilityChecker =
        GoogleApiAvailabilityCheckerImpl(context)

    @Singleton
    @Provides
    fun provideLocationRepository(context: Context): LocationRepository =
        LocationRepositoryImpl(context)
}