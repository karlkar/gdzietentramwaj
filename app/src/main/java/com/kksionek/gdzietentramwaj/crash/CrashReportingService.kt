package com.kksionek.gdzietentramwaj.crash

interface CrashReportingService {

    fun reportCrash(throwable: Throwable, message: String? = null)
}