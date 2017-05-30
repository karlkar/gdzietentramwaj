package com.kksionek.gdzietentramwaj.di;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.kksionek.gdzietentramwaj.DataSource.Room.MyDatabase;
import com.kksionek.gdzietentramwaj.DataSource.Room.TramDao;
import com.kksionek.gdzietentramwaj.DataSource.TramInterface;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class AppModule {

    private final Context mContext;

    public AppModule(Context context) {
        mContext = context;
    }

    @Singleton @Provides
    Context provideContext() {
        return mContext;
    }

    @Singleton @Provides
    MyDatabase getMyDatabase(Context context) {
        return Room.databaseBuilder(context, MyDatabase.class, "favorites.db").build();
    }

    @Singleton @Provides
    TramDao getTramDao(MyDatabase myDatabase) {
        return myDatabase.tramDao();
    }

    @Singleton @Provides
    TramInterface provideTramInterface(OkHttpClient okHttpClient, CallAdapter.Factory rxAdapter) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .addCallAdapterFactory(rxAdapter)
                .baseUrl("https://api.um.warszawa.pl/")
                .build().create(TramInterface.class);
    }

    @Singleton @Provides
    OkHttpClient provideOkHttpClient() {
        // DEBUG
//        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
//                .addInterceptor(interceptor)
                .build();
    }

    @Singleton @Provides
    CallAdapter.Factory getCallAdapterFactory() {
        return RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());
    }
}
