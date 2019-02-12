package com.kksionek.gdzietentramwaj;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.kksionek.gdzietentramwaj.di.AppComponent;
import com.kksionek.gdzietentramwaj.di.AppModule;
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent;

import java.io.IOException;

import io.fabric.sdk.android.Fabric;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;

public class TramApplication extends MultiDexApplication {

    private static final String TAG = "TramApplication";

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(
                this,
                new Crashlytics.Builder()
                        .core(
                                new CrashlyticsCore.Builder()
                                        .disabled(BuildConfig.DEBUG)
                                        .build())
                        .build());

        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if (e instanceof IOException) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if (e instanceof InterruptedException) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Log.e(TAG, "UndeliverableException happened (probably bug): " + e.getMessage());
                Crashlytics.log("UndeliverableException happened (probably bug)");
                Crashlytics.logException(e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Log.e(TAG, "UndeliverableException happened (bug in RxJava or in a custom operator): " + e.getMessage());
                Crashlytics.log("UndeliverableException happened (bug in RxJava or in a custom operator)");
                Crashlytics.logException(e);
            }
        });

        sAppComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(getApplicationContext()))
                .build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
