package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.google.android.gms.tasks.OnCanceledListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val KEY = "key"

class WarsawApikeyRepositoryImplTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val onCanceledCaptor = argumentCaptor<OnCanceledListener>()
    private val onSuccessCaptor = argumentCaptor<OnSuccessListener<Boolean>>()
    private val onFailureCaptor = argumentCaptor<OnFailureListener>()

    private val fetchAndActivateTask: Task<Boolean> = mock()
    private val firebaseRemoteConfig: FirebaseRemoteConfig = mock {
        on { fetchAndActivate() } doReturn fetchAndActivateTask
        on { getString(REMOTE_CONFIG_WARSAW_API_KEY) } doReturn KEY
    }

    private lateinit var tested: WarsawApikeyRepositoryImpl

    @Before
    fun setup() {
        whenever(fetchAndActivateTask.addOnCanceledListener(onCanceledCaptor.capture()))
            .thenReturn(fetchAndActivateTask)
        whenever(fetchAndActivateTask.addOnSuccessListener(onSuccessCaptor.capture()))
            .thenReturn(fetchAndActivateTask)
        whenever(fetchAndActivateTask.addOnFailureListener(onFailureCaptor.capture()))
            .thenReturn(fetchAndActivateTask)

        tested = WarsawApikeyRepositoryImpl(firebaseRemoteConfig)
    }

    @Test
    fun `should return key from repository when fetching the config succeeded`() {
        // when
        val observer = tested.apikey.test()
        onSuccessCaptor.firstValue.onSuccess(true)

        // then
        observer
            .assertValue { it == KEY }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return default key from repository when fetching the config failed`() {
        // given
        val error: IOException = mock()

        // when
        val observer = tested.apikey.test()
        onFailureCaptor.firstValue.onFailure(error)

        // then
        observer
            .assertValue { it == DEFAULT_API_KEY }
            .assertNoErrors()
            .assertComplete()
    }

//    @Test
//    fun `should report error to crashlytics when fetching the config failed`() {
//        // given
//        val error: IOException = mock()
//        // when
//        tested.apikey.test()
//        onFailureCaptor.firstValue.onFailure(error)
//
//        // then
//        verify(crashlyticsReportingService).reportCrash(error)
//    }

    @Test
    fun `should return default key from repository when fetched key is empty`() {
        // given
        whenever(firebaseRemoteConfig.getString(REMOTE_CONFIG_WARSAW_API_KEY)).thenReturn("")

        // when
        val observer = tested.apikey.test()
        onSuccessCaptor.firstValue.onSuccess(true)

        // then
        observer
            .assertValue { it == DEFAULT_API_KEY }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should ask firebase for key only once when requested multiple times`() {
        // given
        tested.apikey.test()
        onSuccessCaptor.firstValue.onSuccess(true)

        // when
        tested.apikey.test()

        // then
        verify(firebaseRemoteConfig, times(1)).fetchAndActivate()
        verify(firebaseRemoteConfig, times(1)).getString(any())
    }

}