package com.kksionek.gdzietentramwaj.map.view

import android.support.annotation.StringRes

sealed class UiState<T> {

    class InProgress<T> : UiState<T>()

    data class Success<T>(
        val data: T
    ) : UiState<T>()

    data class Error<T>(@StringRes val message: Int, val args: List<String?> = emptyList()) :
        UiState<T>()
}
