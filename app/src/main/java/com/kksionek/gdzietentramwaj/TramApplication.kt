package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import com.kksionek.gdzietentramwaj.di.AppComponent
import com.kksionek.gdzietentramwaj.di.AppModule
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent

class TramApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {

        private const val TAG = "TramApplication"
    }
}
