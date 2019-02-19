package com.kksionek.gdzietentramwaj.di

import android.arch.persistence.room.Room
import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.kksionek.gdzietentramwaj.CrashReportingService
import com.kksionek.gdzietentramwaj.CrashlyticsCrashReportingService
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.dataSource.room.MyDatabase
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
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
}
