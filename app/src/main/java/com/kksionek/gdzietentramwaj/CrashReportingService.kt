package com.kksionek.gdzietentramwaj

interface CrashReportingService {

    fun reportCrash(throwable: Throwable, message: String? = null)
}