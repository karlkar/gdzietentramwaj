package com.kksionek.gdzietentramwaj.map.model

data class DifficultiesState(
    val isSupported: Boolean,
    val difficultiesEntities: List<DifficultiesEntity>
)