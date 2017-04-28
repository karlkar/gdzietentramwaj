package com.kksionek.gdzietentramwaj;

import android.app.Application;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.kksionek.gdzietentramwaj.data.TramInterface;
import com.kksionek.gdzietentramwaj.model.Geolocalizer;

import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TramApplication extends Application {

    private static TramApplication sInstance;

    private TramInterface mTramInterface;
    private Geolocalizer mGeolocalizer;

    public TramInterface getTramInterface() {
        return mTramInterface;
    }

    public Geolocalizer getGeolocalizer() {
        return mGeolocalizer;
    }

    public static TramApplication getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

        // DEBUG
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(interceptor)
                .build();

        RxJava2CallAdapterFactory rxAdapter = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());

        mTramInterface = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .addCallAdapterFactory(rxAdapter)
                .baseUrl("https://api.um.warszawa.pl/")
                .build().create(TramInterface.class);

        mGeolocalizer = new Geolocalizer(this);
        mGeolocalizer.onStart();
    }

    @Override
    public void onTerminate() {
        mGeolocalizer.onStop();
        super.onTerminate();
        sInstance = null;
    }
}
