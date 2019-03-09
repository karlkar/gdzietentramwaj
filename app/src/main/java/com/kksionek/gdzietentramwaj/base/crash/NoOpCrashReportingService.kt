package com.kksionek.gdzietentramwaj.base.crash

class NoOpCrashReportingService : CrashReportingService {
    override fun reportCrash(throwable: Throwable, message: String?) {
        // NOOP
    }
}