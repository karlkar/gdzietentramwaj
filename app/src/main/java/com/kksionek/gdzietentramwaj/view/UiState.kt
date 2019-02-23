package com.kksionek.gdzietentramwaj.view

sealed class UiState {
    object InProgress: UiState()
    data class Success(
        val data: List<TramMarker>,
        val animate: Boolean
    ): UiState()
    data class Error(val throwable: Throwable, val show: Boolean): UiState()
}