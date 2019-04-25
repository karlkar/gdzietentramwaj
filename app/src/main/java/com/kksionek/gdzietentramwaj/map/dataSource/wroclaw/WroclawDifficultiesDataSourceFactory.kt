package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.base.dataSource.InterfaceBuilder
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import javax.inject.Inject

private const val BASE_URL = "http://mpk.wroc.pl"

class WroclawDifficultiesDataSourceFactory @Inject constructor(
    private val interfaceBuilder: InterfaceBuilder,
    private val crashReportingService: CrashReportingService
) {

    fun create(): DifficultiesDataSource {
        val mpkDifficultiesInterface =
            interfaceBuilder.create(BASE_URL, WroclawDifficultiesInterface::class)
        return WroclawDifficultiesDataSource(mpkDifficultiesInterface, crashReportingService)
    }
}