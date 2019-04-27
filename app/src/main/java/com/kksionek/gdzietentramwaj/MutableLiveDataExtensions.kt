package com.kksionek.gdzietentramwaj

import androidx.lifecycle.MutableLiveData

fun <T> MutableLiveData<T>.initWith(initVal: T): MutableLiveData<T> {
    this.value = initVal
    return this
}