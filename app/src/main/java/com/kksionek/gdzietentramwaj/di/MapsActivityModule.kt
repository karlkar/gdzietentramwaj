package com.kksionek.gdzietentramwaj.di

import android.arch.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.dataSource.TramInterface
import com.kksionek.gdzietentramwaj.view.AdProvider
import com.kksionek.gdzietentramwaj.view.AdProviderInterface
import com.kksionek.gdzietentramwaj.viewModel.MapsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    internal fun provideAdProvider(): AdProviderInterface = AdProvider()
}