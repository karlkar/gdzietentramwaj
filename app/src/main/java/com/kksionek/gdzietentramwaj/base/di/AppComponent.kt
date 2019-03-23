package com.kksionek.gdzietentramwaj.base.di

import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.favorite.di.FavoriteLinesActivityViewModelModule
import com.kksionek.gdzietentramwaj.favorite.view.FavoriteLinesActivity
import com.kksionek.gdzietentramwaj.map.di.MapsActivityModule
import com.kksionek.gdzietentramwaj.map.view.MapsActivity
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
    fun inject(application: TramApplication)
}
