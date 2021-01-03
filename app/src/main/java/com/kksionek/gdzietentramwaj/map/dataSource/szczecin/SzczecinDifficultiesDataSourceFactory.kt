package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

class SzczecinDifficultiesDataSourceFactory @Inject constructor() {

    fun create(): DifficultiesDataSource = SzczecinDifficultiesDataSource()
}