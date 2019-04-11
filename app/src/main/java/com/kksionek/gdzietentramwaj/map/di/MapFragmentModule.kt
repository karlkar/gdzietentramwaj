package com.kksionek.gdzietentramwaj.map.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
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

    @Singleton
    @Provides
    internal fun provideRetrofitBuilder( // TODO Change to some class like interfaceBuilder
        okHttpClient: OkHttpClient,
        rxAdapter: CallAdapter.Factory
    ): Retrofit.Builder = Retrofit.Builder()
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .addCallAdapterFactory(rxAdapter)
    
    @Singleton
    @Provides
    internal fun provideIconSettingsProvider(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): IconSettingsProvider = settingsRepositoryImpl

    @Singleton
    @Provides
    internal fun provideMapSettingsProvider(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): MapSettingsProvider = settingsRepositoryImpl
}