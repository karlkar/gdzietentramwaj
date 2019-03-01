package com.kksionek.gdzietentramwaj

import com.kksionek.gdzietentramwaj.map.dataSource.NetworkOperationResult
import io.reactivex.Single

fun <T> Single<T>.toNetworkOperationResult(): Single<NetworkOperationResult<T>> =
    map { data -> NetworkOperationResult.Success(data) as NetworkOperationResult<T> }
        .onErrorReturn { NetworkOperationResult.Error(it) }