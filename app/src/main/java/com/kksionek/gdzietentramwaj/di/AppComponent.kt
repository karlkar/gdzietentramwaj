package com.kksionek.gdzietentramwaj.di

import com.kksionek.gdzietentramwaj.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.view.MapsActivity
import dagger.Component
import javax.inject.Singleton

@ActivityScope
@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelFactoryModule::class,
        MapsActivityModule::class,
        FavoriteLinesActivityViewModelModule::class
    ]
)
interface AppComponent {
    fun inject(activity: MapsActivity)
    fun inject(activity: FavoriteLinesActivity)
}
