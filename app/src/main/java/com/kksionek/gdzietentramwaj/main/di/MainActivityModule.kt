package com.kksionek.gdzietentramwaj.main.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.di.ActivityScope
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepositoryImpl
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityCheckerImpl
import com.kksionek.gdzietentramwaj.main.viewModel.MainViewModel
import com.kksionek.gdzietentramwaj.map.repository.FusedLocationRepository
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.view.AdProvider
import com.kksionek.gdzietentramwaj.view.AdProviderImpl
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

    @ActivityScope
    @Provides
    internal fun provideAdProvider(application: TramApplication): AdProvider =
        AdProviderImpl(application)

    @Singleton
    @Provides
    internal fun provideAppUpdateManager(context: Context): AppUpdateManager =
        AppUpdateManagerFactory.create(context)

    @Singleton
    @Provides
    fun provideAppUpdateRepository(appUpdateManager: AppUpdateManager): AppUpdateRepository =
        AppUpdateRepositoryImpl(appUpdateManager)

    @Singleton
    @Provides
    fun provideGoogleApiAvailabilityChecker(context: Context): GoogleApiAvailabilityChecker =
        GoogleApiAvailabilityCheckerImpl(context)

    @Singleton
    @Provides
    fun provideLocationRepository(context: Context): LocationRepository =
        FusedLocationRepository(context)
}