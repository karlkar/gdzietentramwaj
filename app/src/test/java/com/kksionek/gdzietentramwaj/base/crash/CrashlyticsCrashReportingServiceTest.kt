package com.kksionek.gdzietentramwaj.base.crash

import com.crashlytics.android.core.CrashlyticsCore
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.io.IOException

class CrashlyticsCrashReportingServiceTest {

    private val crashlyticsCore: CrashlyticsCore = mock()

    private val tested: CrashlyticsCrashReportingService =
        CrashlyticsCrashReportingService(crashlyticsCore)

    @Test
    fun `should log exception to crashlytics when requested`() {
        // given
        val exception: IOException = mock()

        // when
        tested.reportCrash(exception)

        // then
        verify(crashlyticsCore).logException(exception)
    }

    @Test
    fun `given message is provided should log message when logging exception when requested`() {
        // given
        val message = "Message"
        val exception: IOException = mock()

        // when
        tested.reportCrash(exception, message)

        // then
        verify(crashlyticsCore).log(message)
        verify(crashlyticsCore).logException(exception)
    }
}