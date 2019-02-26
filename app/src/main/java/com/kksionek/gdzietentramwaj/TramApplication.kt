package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import com.kksionek.gdzietentramwaj.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.di.AppComponent
import com.kksionek.gdzietentramwaj.di.AppModule
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent
import javax.inject.Inject

class TramApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    @Inject
    lateinit var crashReportingService: CrashReportingService

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        appComponent.inject(this)
    }

    companion object {

        private const val TAG = "TramApplication"
    }
}
