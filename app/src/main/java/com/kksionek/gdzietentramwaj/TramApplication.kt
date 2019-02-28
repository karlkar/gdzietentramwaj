package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import com.kksionek.gdzietentramwaj.base.di.AppComponent
import com.kksionek.gdzietentramwaj.base.di.AppModule
import com.kksionek.gdzietentramwaj.base.di.DaggerAppComponent

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
