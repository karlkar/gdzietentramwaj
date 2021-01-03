package com.kksionek.gdzietentramwaj

import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

fun <T: Any> Single<T>.toNetworkOperationResult(): Single<NetworkOperationResult<T>> =
    map { data -> NetworkOperationResult.Success(data) as NetworkOperationResult<T> }
        .onErrorReturn { NetworkOperationResult.Error(it) }

fun Disposable.addToDisposable(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}