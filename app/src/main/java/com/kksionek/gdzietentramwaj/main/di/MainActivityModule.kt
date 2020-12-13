package com.kksionek.gdzietentramwaj.main.di

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepository
import com.kksionek.gdzietentramwaj.main.repository.AppUpdateRepositoryImpl
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityChecker
import com.kksionek.gdzietentramwaj.main.repository.GoogleApiAvailabilityCheckerImpl
import com.kksionek.gdzietentramwaj.map.repository.FusedLocationRepository
import com.kksionek.gdzietentramwaj.map.repository.LocationRepository
import com.kksionek.gdzietentramwaj.map.view.AdProvider
import com.kksionek.gdzietentramwaj.view.AdProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ActivityComponent::class)
@Module
class MainActivityModule {

    @Provides
    internal fun provideAdProvider(application: TramApplication): AdProvider =
        AdProviderImpl(application)

    @Provides
    internal fun provideAppUpdateManager(@ApplicationContext context: Context): AppUpdateManager =
        AppUpdateManagerFactory.create(context)

    @Provides
    fun provideAppUpdateRepository(appUpdateManager: AppUpdateManager): AppUpdateRepository =
        AppUpdateRepositoryImpl(appUpdateManager)

    @Provides
    fun provideGoogleApiAvailabilityChecker(@ApplicationContext context: Context): GoogleApiAvailabilityChecker =
        GoogleApiAvailabilityCheckerImpl(context)

    @Provides
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository =
        FusedLocationRepository(context)
}