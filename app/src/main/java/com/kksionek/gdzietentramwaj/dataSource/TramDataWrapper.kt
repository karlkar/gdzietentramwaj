package com.kksionek.gdzietentramwaj.dataSource

sealed class TramDataWrapper {
    data class Success(val tramDataHashMap: Map<String, TramData>) : TramDataWrapper()

    data class Error(val throwable: Throwable) : TramDataWrapper()

    object InProgress : TramDataWrapper()
}
