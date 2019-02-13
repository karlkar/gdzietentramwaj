package com.kksionek.gdzietentramwaj

import android.support.multidex.MultiDexApplication
import android.util.Log
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.kksionek.gdzietentramwaj.di.AppComponent
import com.kksionek.gdzietentramwaj.di.AppModule
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent
import io.fabric.sdk.android.Fabric
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import java.io.IOException

class TramApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        Fabric.with(
            this,
            Crashlytics.Builder()
                .core(
                    CrashlyticsCore.Builder()
                        .disabled(BuildConfig.DEBUG)
                        .build()
                )
                .build()
        )

        RxJavaPlugins.setErrorHandler { e ->
            val cause = if (e is UndeliverableException) e.cause else e
            if (cause is IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return@setErrorHandler
            }
            if (cause is InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return@setErrorHandler
            }
            if (cause is NullPointerException || cause is IllegalArgumentException) {
                // that's likely a bug in the application
                Log.e(TAG, "UndeliverableException happened (probably bug): $cause.message}")
                Crashlytics.log("UndeliverableException happened (probably bug)")
                Crashlytics.logException(cause)
                return@setErrorHandler
            }
            if (cause is IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Log.e(
                    TAG,
                    "UndeliverableException happened (bug in RxJava or in a custom operator): ${cause.message}"
                )
                Crashlytics.log("UndeliverableException happened (bug in RxJava or in a custom operator)")
                Crashlytics.logException(cause)
            }
        }

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {

        private const val TAG = "TramApplication"
    }
}
