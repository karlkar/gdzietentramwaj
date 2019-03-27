package com.kksionek.gdzietentramwaj.map.di

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.kksionek.gdzietentramwaj.base.di.ActivityScope
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesInterface
import com.kksionek.gdzietentramwaj.map.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import com.kksionek.gdzietentramwaj.view.AdProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import javax.inject.Singleton

@Module(includes = [MapsActivityModule.MapsViewModelModule::class])
class MapsActivityModule {

    @Module
    interface MapsViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(MapsViewModel::class)
        fun bindMapsViewModel(mapsActivityViewModel: MapsViewModel): ViewModel
    }

    @ActivityScope
    @Provides
    internal fun provideAdProvider(): AdProviderInterface = AdProvider()

    @ActivityScope
    @Provides
    internal fun provideMapsViewSettingsRepository(context: Context): MapsViewSettingsRepository =
        MapsViewSettingsRepositoryImpl(context)

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
    internal fun provideDifficultiesInterface(
        okHttpClient: OkHttpClient,
        rxAdapter: CallAdapter.Factory
    ): DifficultiesInterface {
        return Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .client(okHttpClient)
            .addCallAdapterFactory(rxAdapter)
            .baseUrl("https://www.ztm.waw.pl/")
            .build().create(DifficultiesInterface::class.java)
    }

    @Singleton
    @Provides
    internal fun provideIconSettingsProvider(
        mapsViewSettingsRepository: MapsViewSettingsRepository
    ): IconSettingsProvider = mapsViewSettingsRepository

    @Singleton
    @Provides
    internal fun provideIconSettingsManager(
        mapsViewSettingsRepository: MapsViewSettingsRepository
    ): IconSettingsManager = mapsViewSettingsRepository
}