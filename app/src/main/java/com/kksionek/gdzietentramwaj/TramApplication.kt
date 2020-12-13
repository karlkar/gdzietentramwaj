package com.kksionek.gdzietentramwaj

import androidx.multidex.MultiDexApplication
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.functions.Consumer
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class TramApplication : MultiDexApplication() {

    @Inject
    lateinit var rxJavaErrorHandler: Consumer<in Throwable>

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        RxJavaPlugins.setErrorHandler(rxJavaErrorHandler)
    }
}
