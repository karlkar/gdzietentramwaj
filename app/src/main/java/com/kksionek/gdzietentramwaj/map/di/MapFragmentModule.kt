package com.kksionek.gdzietentramwaj.map.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ActivityScope
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesInterface
import com.kksionek.gdzietentramwaj.map.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
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

@Module(includes = [MapFragmentModule.MapsViewModelModule::class])
class MapFragmentModule {

    @Module
    interface MapsViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(MapsViewModel::class)
        fun bindMapsViewModel(mapsViewModel: MapsViewModel): ViewModel
    }

    @ActivityScope
    @Provides
    internal fun provideAdProvider(): AdProviderInterface = AdProvider()

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
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): IconSettingsProvider = settingsRepositoryImpl
}