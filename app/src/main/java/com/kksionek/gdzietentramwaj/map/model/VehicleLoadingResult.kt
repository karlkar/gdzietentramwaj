package com.kksionek.gdzietentramwaj.map.model

data class VehicleLoadingResult(
    val data: List<VehicleToDrawData>,
    val newData: Boolean = false
)