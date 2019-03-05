package com.kksionek.gdzietentramwaj.map.view

data class BusTramLoading(
    val data: List<TramMarker>,
    val animate: Boolean,
    val newData: Boolean = false
)