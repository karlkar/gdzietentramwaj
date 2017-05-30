package com.kksionek.gdzietentramwaj.di;

import android.content.Context;

import com.kksionek.gdzietentramwaj.Repository.LocationRepository;
import com.kksionek.gdzietentramwaj.Repository.TramRepository;
import com.kksionek.gdzietentramwaj.ViewModel.MainActivityViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    TramRepository getTramRepository();
    LocationRepository getLocationRepository();
    Context getAppContext();
}
