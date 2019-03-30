package com.kksionek.gdzietentramwaj.settings.viewModel

import androidx.lifecycle.ViewModel
import com.kksionek.gdzietentramwaj.map.repository.IconSettingsManager
import com.kksionek.gdzietentramwaj.map.repository.MapSettingsManager
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val iconSettingsManager: IconSettingsManager,
    private val mapSettingsManager: MapSettingsManager
) : ViewModel() {

    var oldIconSetEnabled: Boolean
        get() {
            return iconSettingsManager.isOldIconSetEnabled()
        }
        set(value) {
            iconSettingsManager.setIsOldIconSetEnabled(value)
        }

    var autoZoomEnabled: Boolean
        get() {
            return mapSettingsManager.isAutoZoomEnabled()
        }
        set(value) {
            mapSettingsManager.setAutoZoomEnabled(value)
        }
}