package com.kksionek.gdzietentramwaj.base.crash

import com.crashlytics.android.core.CrashlyticsCore

class CrashlyticsCrashReportingService(
    private val crashlyticsCoreInstance: CrashlyticsCore
): CrashReportingService {

    override fun reportCrash(throwable: Throwable, message: String?) {
        message?.let {
            crashlyticsCoreInstance.log(message)
        }
        crashlyticsCoreInstance.logException(throwable)
    }
}