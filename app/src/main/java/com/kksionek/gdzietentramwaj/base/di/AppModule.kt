package com.kksionek.gdzietentramwaj.base.di

import android.arch.persistence.room.Room
import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.crash.CrashlyticsCrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.base.dataSource.room.MyDatabase
import com.kksionek.gdzietentramwaj.base.dataSource.room.TramDao
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    internal fun provideTramInterface(
        okHttpClient: OkHttpClient,
        rxAdapter: CallAdapter.Factory
    ): TramInterface {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .addCallAdapterFactory(rxAdapter)
            .baseUrl("https://api.um.warszawa.pl/")
            .build().create(TramInterface::class.java)
    }

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
