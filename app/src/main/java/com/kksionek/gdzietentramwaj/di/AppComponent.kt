package com.kksionek.gdzietentramwaj.di

import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.view.MapsActivity
import dagger.Component
import javax.inject.Singleton

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
    fun inject(application: TramApplication)
}
