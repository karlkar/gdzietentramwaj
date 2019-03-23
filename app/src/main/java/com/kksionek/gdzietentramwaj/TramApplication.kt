package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import com.kksionek.gdzietentramwaj.base.di.AppComponent
import com.kksionek.gdzietentramwaj.base.di.AppModule
import com.kksionek.gdzietentramwaj.base.di.DaggerAppComponent
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import javax.inject.Inject

class TramApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var rxJavaErrorHandler: Consumer<in Throwable>

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        appComponent.inject(this)

        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
    }

    companion object {

        private const val TAG = "TramApplication"
    }
}
