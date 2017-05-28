package com.kksionek.gdzietentramwaj;

import android.app.Application;

import com.kksionek.gdzietentramwaj.di.AppComponent;
import com.kksionek.gdzietentramwaj.di.AppModule;
import com.kksionek.gdzietentramwaj.di.DaggerAppComponent;

public class TramApplication extends Application {

    private static AppComponent sAppComponent;

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
