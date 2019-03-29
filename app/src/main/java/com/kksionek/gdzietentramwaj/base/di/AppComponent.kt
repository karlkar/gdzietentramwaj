package com.kksionek.gdzietentramwaj.base.di

import com.kksionek.gdzietentramwaj.TramApplication
import com.kksionek.gdzietentramwaj.favorite.di.FavoriteLinesActivityViewModelModule
import com.kksionek.gdzietentramwaj.favorite.view.FavoriteFragment
import com.kksionek.gdzietentramwaj.main.di.MainActivityModule
import com.kksionek.gdzietentramwaj.main.view.MainActivity
import com.kksionek.gdzietentramwaj.map.di.MapFragmentModule
import com.kksionek.gdzietentramwaj.map.view.MapFragment
import com.kksionek.gdzietentramwaj.settings.di.SettingsFragmentModule
import com.kksionek.gdzietentramwaj.settings.view.SettingsFragment
import dagger.Component
import javax.inject.Singleton

@ActivityScope
@Singleton
@Component(
    modules = [
        AppModule::class,
        ViewModelFactoryModule::class,
        MapFragmentModule::class,
        MainActivityModule::class,
        SettingsFragmentModule::class,
        FavoriteLinesActivityViewModelModule::class
    ]
)
interface AppComponent {
    fun inject(activity: MainActivity)
    fun inject(fragment: MapFragment)
    fun inject(fragment: SettingsFragment)
    fun inject(fragment: FavoriteFragment)
    fun inject(application: TramApplication)
}
