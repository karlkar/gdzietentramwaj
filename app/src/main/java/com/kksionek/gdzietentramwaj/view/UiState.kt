package com.kksionek.gdzietentramwaj.view

import android.support.annotation.StringRes
import com.kksionek.gdzietentramwaj.viewModel.MapsViewModel

sealed class UiState {

    object InProgress : UiState()

    data class Success(
        val data: Map<MapsViewModel.TramAction, List<TramMarker>>,
        val animate: Boolean
    ) : UiState()

    data class Error(@StringRes val message: Int, val args: List<String?> = emptyList()) : UiState()
}