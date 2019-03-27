package com.kksionek.gdzietentramwaj.settings.viewModel

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val iconSettingsManager: IconSettingsManager
) : ViewModel() {

    fun isOldIconSetEnabled(): Boolean = iconSettingsManager.isOldIconSetEnabled()

    fun setIsOldIconSetEnabled(enabled: Boolean) =
        iconSettingsManager.setIsOldIconSetEnabled(enabled)
}