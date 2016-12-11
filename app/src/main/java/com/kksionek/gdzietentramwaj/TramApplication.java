package com.kksionek.gdzietentramwaj;

import android.app.Application;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TramApplication extends Application {

    TramInterface mTramInterface;

    public TramInterface getTramInterface() {
        return mTramInterface;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // DEBUG
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        mTramInterface = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
//                .client(okHttpClient)
                .baseUrl("https://api.um.warszawa.pl/")
                .build().create(TramInterface.class);
    }
}
