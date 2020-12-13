package com.kksionek.gdzietentramwaj.base.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.crash.CrashlyticsCrashReportingService
import com.kksionek.gdzietentramwaj.base.crash.NoOpCrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.MyDatabase
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.base.view.ImageLoader
import com.kksionek.gdzietentramwaj.base.view.PicassoImageLoader
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter

@InstallIn(SingletonComponent::class)
@Module
class AppModule {

    @Provides
    internal fun provideTramApplication(application: Application): TramApplication =
        application as TramApplication

    @Provides
    internal fun provideCallAdapterFactory(): CallAdapter.Factory =
        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())

    @Provides
    internal fun provideCrashlyticsInstance(): FirebaseCrashlytics =
        FirebaseCrashlytics.getInstance()

    @Provides
    internal fun provideCrashReportingService(crashlytics: FirebaseCrashlytics): CrashReportingService {
        return if (BuildConfig.DEBUG) {
            NoOpCrashReportingService()
        } else {
            CrashlyticsCrashReportingService(crashlytics)
        }
    }

    @Provides
    internal fun getMyDatabase(@ApplicationContext context: Context): MyDatabase {
        return Room.databaseBuilder(context, MyDatabase::class.java, "favorites.db")
            .addMigrations(
                MyDatabase.MIGRATION_1_2,
                MyDatabase.MIGRATION_2_3,
                MyDatabase.MIGRATION_3_4,
                MyDatabase.MIGRATION_4_5
            )
            .build()
    }

    @Provides
    internal fun getTramDao(myDatabase: MyDatabase): TramDao = myDatabase.tramDao()

    @Provides
    internal fun provideOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()
    }

    @Provides
    internal fun providePicasso(): Picasso = Picasso.get()

    @Provides
    internal fun provideImageLoader(picasso: Picasso): ImageLoader = PicassoImageLoader(picasso)

    @Provides
    internal fun provideRxJavaErrorHandler(
        crashReportingService: CrashReportingService
    ): Consumer<in Throwable> = Consumer {
        crashReportingService.reportCrash(it, "Global Error Handler")
    }

    @Provides
    internal fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepositoryImpl =
        SettingsRepositoryImpl(context)

    @Provides
    internal fun provideMapsViewSettingsRepository(settingsRepositoryImpl: SettingsRepositoryImpl): MapsViewSettingsRepository =
        settingsRepositoryImpl
}
