package com.kksionek.gdzietentramwaj.map.dataSource

sealed class NetworkOperationResult<T> {

    data class Success<T>(val tramDataHashMap: T) : NetworkOperationResult<T>()

    data class Error<T>(val throwable: Throwable) : NetworkOperationResult<T>()
}
