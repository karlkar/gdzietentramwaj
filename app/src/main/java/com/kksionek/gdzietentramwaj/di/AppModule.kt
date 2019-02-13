package com.kksionek.gdzietentramwaj.di

import android.arch.persistence.room.Room
import android.content.Context
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.dataSource.room.MyDatabase
import com.kksionek.gdzietentramwaj.dataSource.room.TramDao
import com.kksionek.gdzietentramwaj.view.AdProvider
import com.kksionek.gdzietentramwaj.view.AdProviderInterface
import dagger.Module
import dagger.Provides
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
class AppModule(private val context: Context) {

    @Singleton
    @Provides
    internal fun provideCallAdapterFactory(): CallAdapter.Factory =
        RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())

    @Singleton
    @Provides
    internal fun provideContext(): Context {
        return context
    }

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
    internal fun getTramDao(myDatabase: MyDatabase): TramDao {
        return myDatabase.tramDao()
    }

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

    @Singleton
    @Provides
    internal fun provideAdProvider(): AdProviderInterface {
        return AdProvider()
    }
}