package com.kksionek.gdzietentramwaj.base.crash

interface CrashReportingService {

    fun reportCrash(throwable: Throwable, message: String? = null)
}