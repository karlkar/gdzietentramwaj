package com.kksionek.gdzietentramwaj.base.crash

import com.crashlytics.android.Crashlytics

class CrashlyticsCrashReportingService : CrashReportingService {

    override fun reportCrash(throwable: Throwable, message: String?) {
        message?.let {
            Crashlytics.log(message)
        }
        Crashlytics.logException(throwable)
    }
}