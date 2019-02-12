package com.kksionek.gdzietentramwaj.di

import android.content.Context
import com.kksionek.gdzietentramwaj.Repository.LocationRepository
import com.kksionek.gdzietentramwaj.Repository.TramRepository
import com.kksionek.gdzietentramwaj.view.MapsActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    val tramRepository: TramRepository
    val locationRepository: LocationRepository
    val appContext: Context

    fun inject(activity: MapsActivity)
}
