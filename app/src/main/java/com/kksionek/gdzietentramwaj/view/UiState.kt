package com.kksionek.gdzietentramwaj.view

import android.support.annotation.StringRes

sealed class UiState {

    object InProgress : UiState()

    data class Success(
        val data: List<TramMarker>,
        val animate: Boolean,
        val newData: Boolean = false
    ) : UiState()

    data class Error(@StringRes val message: Int, val args: List<String?> = emptyList()) : UiState()
}