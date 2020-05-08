package com.kksionek.gdzietentramwaj.map.view

import com.kksionek.gdzietentramwaj.map.model.VehicleToDrawData

data class BusTramLoading(
    val data: List<VehicleToDrawData>,
    val animate: Boolean,
    val newData: Boolean = false
)