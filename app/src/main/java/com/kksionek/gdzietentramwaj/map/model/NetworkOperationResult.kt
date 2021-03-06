package com.kksionek.gdzietentramwaj.map.model

sealed class NetworkOperationResult<T> {

    class InProgress<T> : NetworkOperationResult<T>()

    data class Success<T>(val data: T) : NetworkOperationResult<T>()

    data class Error<T>(val throwable: Throwable) : NetworkOperationResult<T>()
}
