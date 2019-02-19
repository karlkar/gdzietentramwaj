package com.kksionek.gdzietentramwaj.di

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.kksionek.gdzietentramwaj.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.repository.MapsViewSettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.view.AdProvider
import com.kksionek.gdzietentramwaj.view.AdProviderInterface
import com.kksionek.gdzietentramwaj.viewModel.MapsViewModel
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

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
}