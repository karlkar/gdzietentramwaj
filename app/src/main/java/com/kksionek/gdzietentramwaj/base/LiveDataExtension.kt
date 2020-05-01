package com.kksionek.gdzietentramwaj.base

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (value: T) -> Unit) {
    this.observe(owner, Observer {
        it?.let { observer.invoke(it) }
    })
}

private class SingleObserver<T>(
    private val liveData: LiveData<T>,
    private val observer: (value: T) -> Unit
) : Observer<T?> {

    override fun onChanged(it: T?) {
        it?.let {
            observer.invoke(it)
            liveData.removeObserver(this)
        }
    }
}

fun <T> LiveData<T>.observeNonNullOneEvent(owner: LifecycleOwner, observer: (value: T) -> Unit) {
    this.observe(owner, SingleObserver(this, observer))
}