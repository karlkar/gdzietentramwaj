package com.kksionek.gdzietentramwaj.base.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics

class CrashlyticsCrashReportingService(
    private val crashlyticsCoreInstance: FirebaseCrashlytics
): CrashReportingService {

    override fun reportCrash(throwable: Throwable, message: String?) {
        message?.let {
            crashlyticsCoreInstance.log(message)
        }
        crashlyticsCoreInstance.recordException(throwable)
    }
}