package com.kksionek.gdzietentramwaj.base.di

import android.arch.persistence.room.Room
import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.crash.CrashlyticsCrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.MyDatabase
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import javax.inject.Singleton

@Module
class AppModule(private val application: TramApplication) {

    @Singleton
    @Provides
    internal fun provideCallAdapterFactory(): CallAdapter.Factory =
        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())

    @Singleton
    @Provides
    internal fun provideApplication(): TramApplication = application

    @Singleton
    @Provides
    internal fun provideContext(): Context = application

    @Singleton
    @Provides
    internal fun provideCrashReportingService(): CrashReportingService =
        CrashlyticsCrashReportingService()

    @Singleton
    @Provides
    internal fun getMyDatabase(context: Context): MyDatabase {
        return Room.databaseBuilder(context, MyDatabase::class.java, "favorites.db")
            .addMigrations(
                MyDatabase.MIGRATION_1_2,
                MyDatabase.MIGRATION_2_3,
                MyDatabase.MIGRATION_3_4
            )
            .build()
    }

    @Singleton
    @Provides
    internal fun getTramDao(myDatabase: MyDatabase): TramDao = myDatabase.tramDao()

    @Singleton
    @Provides
    internal fun provideOkHttpClient(): OkHttpClient {
        // DEBUG
        //        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return OkHttpClient.Builder()
            //                .addInterceptor(interceptor)
            .build()
    }
}
