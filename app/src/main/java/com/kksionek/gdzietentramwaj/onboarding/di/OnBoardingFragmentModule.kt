package com.kksionek.gdzietentramwaj.onboarding.di

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.base.di.ViewModelKey
import com.kksionek.gdzietentramwaj.onboarding.viewmodel.OnBoardingViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module(includes = [OnBoardingFragmentModule.OnBoardingFragmentViewModelModule::class])
class OnBoardingFragmentModule {

    @Module
    interface OnBoardingFragmentViewModelModule {
        @Binds
        @IntoMap
        @ViewModelKey(OnBoardingViewModel::class)
        fun bindOnBoardingViewModel(onBoardingViewModel: OnBoardingViewModel): ViewModel
    }
}