package com.kksionek.gdzietentramwaj.map.di

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.kksionek.gdzietentramwaj.base.di.ActivityScope
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepository
import com.kksionek.gdzietentramwaj.map.repository.MapsViewSettingsRepositoryImpl
import com.kksionek.gdzietentramwaj.map.view.AdProviderInterface
import com.kksionek.gdzietentramwaj.map.viewModel.MapsViewModel
import com.kksionek.gdzietentramwaj.view.AdProvider
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