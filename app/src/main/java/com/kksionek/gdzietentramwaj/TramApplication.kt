package com.kksionek.gdzietentramwaj

import androidx.multidex.MultiDexApplication
import com.kksionek.gdzietentramwaj.base.di.AppComponent
import com.kksionek.gdzietentramwaj.base.di.AppModule
import com.kksionek.gdzietentramwaj.base.di.DaggerAppComponent
import com.kksionek.gdzietentramwaj.map.view.AdProvider
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class TramApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var rxJavaErrorHandler: Consumer<in Throwable>

    // injected here in order to call initialize as soon as possible
    @Inject
    lateinit var adProvider: AdProvider

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        appComponent.inject(this)

        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
    }
}
