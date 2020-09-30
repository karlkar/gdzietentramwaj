package com.kksionek.gdzietentramwaj.base.crash

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.io.IOException

class CrashlyticsCrashReportingServiceTest {

    private val firebaseCrashlytics: FirebaseCrashlytics = mock()

    private val tested: CrashlyticsCrashReportingService =
        CrashlyticsCrashReportingService(firebaseCrashlytics)

    @Test
    fun `should log exception to crashlytics when requested`() {
        // given
        val exception: IOException = mock()

        // when
        tested.reportCrash(exception)

        // then
        verify(firebaseCrashlytics).recordException(exception)
    }

    @Test
    fun `given message is provided should log message when logging exception when requested`() {
        // given
        val message = "Message"
        val exception: IOException = mock()

        // when
        tested.reportCrash(exception, message)

        // then
        verify(firebaseCrashlytics).log(message)
        verify(firebaseCrashlytics).recordException(exception)
    }
}