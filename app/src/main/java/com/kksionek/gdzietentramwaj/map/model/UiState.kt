package com.kksionek.gdzietentramwaj.map.model

import androidx.annotation.StringRes

sealed class UiState<out T> {

    class InProgress<T> : UiState<T>()

    data class Success<T>(
        val data: T
    ) : UiState<T>()

    data class Error<T>(@StringRes val message: Int, val args: List<String?> = emptyList()) :
        UiState<T>()
}
// TODO: Error should not contain resources, but should contain data describing it