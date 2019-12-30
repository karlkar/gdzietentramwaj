package com.kksionek.gdzietentramwaj.map.di

import androidx.lifecycle.ViewModel
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilderImpl
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawApikeyRepository
import com.kksionek.gdzietentramwaj.map.dataSource.warsaw.WarsawApikeyRepositoryImpl
import com.kksionek.gdzietentramwaj.map.model.SimpleXmlDeserializer
import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import com.kksionek.gdzietentramwaj.map.repository.DifficultiesRepository
import com.kksionek.gdzietentramwaj.map.repository.DifficultiesRepositoryImpl
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsProvider
import com.kksionek.gdzietentramwaj.map.repository.SettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.map.repository.VehiclesRepository
import com.kksionek.gdzietentramwaj.map.repository.VehiclesRepositoryImpl
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import okhttp3.OkHttpClient
import org.simpleframework.xml.core.Persister
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
    internal fun provideInterfaceBuilder(
        okHttpClient: OkHttpClient,
        rxAdapter: CallAdapter.Factory
    ): InterfaceBuilder {
        val retrofitBuilder = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .addCallAdapterFactory(rxAdapter)
        return InterfaceBuilderImpl(retrofitBuilder)
    }

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

    @Singleton
    @Provides
    internal fun provideFirebaseRemoteConfig(): FirebaseRemoteConfig =
        FirebaseRemoteConfig.getInstance()

    @Singleton
    @Provides
    internal fun provideWarsawApikeyRepository(firebaseRemoteConfig: FirebaseRemoteConfig): WarsawApikeyRepository =
        WarsawApikeyRepositoryImpl(firebaseRemoteConfig)

    @Singleton
    @Provides
    fun providePersister(): Persister = Persister()

    @Singleton
    @Provides
    internal fun provideXmlDeserializer(persister: Persister): XmlDeserializer =
        SimpleXmlDeserializer(persister)

    @Provides
    internal fun provideDifficultiesRepository(
        difficultiesDataSourceFactory: DifficultiesDataSourceFactory
    ): DifficultiesRepository =
        DifficultiesRepositoryImpl(difficultiesDataSourceFactory)

    @Provides
    internal fun provideVehiclesRepository(
        tramDao: TramDao,
        vehicleDataSourceFactory: VehicleDataSourceFactory
    ): VehiclesRepository =
        VehiclesRepositoryImpl(tramDao, vehicleDataSourceFactory)
}