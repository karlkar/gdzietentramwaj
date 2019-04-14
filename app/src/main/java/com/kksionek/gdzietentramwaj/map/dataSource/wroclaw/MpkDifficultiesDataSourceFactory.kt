package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import retrofit2.Retrofit
import javax.inject.Inject

private const val BASE_URL = "http://mpk.wroc.pl"

class MpkDifficultiesDataSourceFactory @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder,
    private val crashReportingService: CrashReportingService
) {

    fun create(): DifficultiesDataSource {
        val mpkDifficultiesInterface = retrofitBuilder
            .baseUrl(BASE_URL)
            .build()
            .create(MpkDifficultiesInterface::class.java)
        return MpkDifficultiesDataSource(mpkDifficultiesInterface, crashReportingService)
    }
}