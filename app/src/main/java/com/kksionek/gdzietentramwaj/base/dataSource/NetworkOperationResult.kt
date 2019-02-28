package com.kksionek.gdzietentramwaj.base.dataSource

//TODO divide base.dataSource to map and favorite
sealed class NetworkOperationResult<T> {

    data class Success<T>(val tramDataHashMap: T) : NetworkOperationResult<T>()

    data class Error<T>(val throwable: Throwable) : NetworkOperationResult<T>()
}
