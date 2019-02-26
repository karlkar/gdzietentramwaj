package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import android.util.Log
import com.kksionek.gdzietentramwaj.di.AppComponent
import com.kksionek.gdzietentramwaj.di.AppModule
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException
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

        RxJavaPlugins.setErrorHandler { e ->
            val cause = if (e is UndeliverableException) e.cause else e
            when (cause) {
                is IOException ->
                    // fine, irrelevant network problem or API that throws on cancellation
                    return@setErrorHandler
                is InterruptedException ->
                    // fine, some blocking code was interrupted by a dispose call
                    return@setErrorHandler
                is NullPointerException, is IllegalArgumentException -> {
                    // that's likely a bug in the application
                    Log.e(TAG, "UndeliverableException happened (probably bug): $cause.message}")
                    crashReportingService.reportCrash(
                        cause,
                        "UndeliverableException happened (probably bug)"
                    )
                    return@setErrorHandler
                }
                is IllegalStateException -> {
                    // that's a bug in RxJava or in a custom operator
                    Log.e(
                        TAG,
                        "UndeliverableException happened (bug in RxJava or in a custom operator): ${cause.message}"
                    )
                    crashReportingService.reportCrash(
                        cause,
                        "UndeliverableException happened (bug in RxJava or in a custom operator)"
                    )
                }
            }
        }
    }

    companion object {

        private const val TAG = "TramApplication"
    }
}
