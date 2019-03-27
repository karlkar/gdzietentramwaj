package com.kksionek.gdzietentramwaj.main.viewModel

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.BuildConfig
import com.kksionek.gdzietentramwaj.main.repository.VersionRepository
import javax.inject.Inject

private const val BUILD_VERSION_WELCOME_WINDOW_ADDED = 23

class MainViewModel @Inject constructor(
    private val versionRepository: VersionRepository
): ViewModel() {

    fun shouldShowWelcomeDialog(): Boolean {
        val lastVersion = versionRepository.getPreviouslyLaunchedVersion()
        versionRepository.saveLastLaunchedVersion(BuildConfig.VERSION_CODE)
        return lastVersion < BUILD_VERSION_WELCOME_WINDOW_ADDED
    }
}