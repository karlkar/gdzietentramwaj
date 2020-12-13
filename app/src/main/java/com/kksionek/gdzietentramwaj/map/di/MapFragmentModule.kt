package com.kksionek.gdzietentramwaj.map.di

import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilderImpl
import com.kksionek.gdzietentramwaj.base.dataSource.TramDao
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.dataSource.VehicleDataSourceFactory
import com.kksionek.gdzietentramwaj.map.model.SimpleXmlDeserializer
import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import com.kksionek.gdzietentramwaj.map.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import okhttp3.OkHttpClient
import org.simpleframework.xml.core.Persister
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

@InstallIn(ActivityComponent::class)
@Module
class MapFragmentModule {

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

    @Provides
    internal fun provideIconSettingsProvider(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): IconSettingsProvider = settingsRepositoryImpl

    @Provides
    internal fun provideMapSettingsProvider(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): MapSettingsProvider = settingsRepositoryImpl

    @Provides
    fun providePersister(): Persister = Persister()

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