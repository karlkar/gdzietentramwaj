package com.kksionek.gdzietentramwaj.map.repository

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.dataSource.Cities
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSourceFactory
import com.kksionek.gdzietentramwaj.map.model.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import com.kksionek.gdzietentramwaj.map.model.NetworkOperationResult
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.util.concurrent.TimeUnit

class DifficultiesRepositoryImplTest {

    private val testScheduler = TestScheduler()

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule(testScheduler)

    private val selectedCity = Cities.WARSAW
    private val returnedDifficulty = DifficultiesEntity(null, "Difficulty1", "http://test1.com")
    private val listOfDifficulties = listOf(returnedDifficulty)
    private val difficultiesDataSource: DifficultiesDataSource = mock {
        on { getDifficulties() } doReturn Single.just(
            DifficultiesState(true, listOfDifficulties)
        )
    }
    private val difficultiesDataSourceFactory: DifficultiesDataSourceFactory = mock {
        on { create(selectedCity) } doReturn difficultiesDataSource
    }

    private val tested = DifficultiesRepositoryImpl(difficultiesDataSourceFactory)

    @Before
    fun setup() {
        testScheduler.triggerActions()
    }

    @Test
    fun `should return difficulties from repository when request succeeded`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Success).data.isSupported
                        && it.data.difficultiesEntities.contains(returnedDifficulty)
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not complete the stream when request succeeded`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress as first element when requested`() {
        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return difficulties from repository two times when subscription lasts long enough`() {
        // given
        val subscriptionTime = 70L
        val observer = tested.dataStream(selectedCity).test()

        // when
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Success).data.isSupported
                        && it.data.difficultiesEntities.contains(returnedDifficulty)
            }
            .assertValueAt(3) {
                (it as NetworkOperationResult.Success).data.isSupported
                        && it.data.difficultiesEntities.contains(returnedDifficulty)
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress when reloading difficulties`() {
        // given
        val subscriptionTime = 70L
        val observer = tested.dataStream(selectedCity).test()

        // when
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return difficulties from repository two times when reload was forced`() {
        // given
        val subscriptionTime = 10L
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // when
        tested.forceReload()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Success).data.isSupported
                        && it.data.difficultiesEntities.contains(returnedDifficulty)
            }
            .assertValueAt(3) {
                (it as NetworkOperationResult.Success).data.isSupported
                        && it.data.difficultiesEntities.contains(returnedDifficulty)
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return in progress when reload was forced`() {
        // given
        val subscriptionTime = 10L
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.advanceTimeBy(subscriptionTime, TimeUnit.SECONDS)

        // when
        tested.forceReload()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should return error item when request failed`() {
        // given
        val error: IOException = mock()
        whenever(difficultiesDataSource.getDifficulties()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(1) {
                (it as NetworkOperationResult.Error<DifficultiesState>).throwable === error
            }
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not complete the stream when request failed`() {
        // given
        val error: IOException = mock()
        whenever(difficultiesDataSource.getDifficulties()).thenReturn(Single.error(error))

        // when
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should not emit multiple values when requests force reload in period shorter than 1 minute`() {
        // given
        val observer = tested.dataStream(selectedCity).test()
        testScheduler.triggerActions()

        // when
        tested.forceReload()
        testScheduler.advanceTimeBy(55, TimeUnit.SECONDS)
        tested.forceReload()
        testScheduler.advanceTimeBy(55, TimeUnit.SECONDS)
        tested.forceReload()
        testScheduler.advanceTimeBy(55, TimeUnit.SECONDS)

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Success }
            .assertValueAt(2) { it is NetworkOperationResult.InProgress }
            .assertValueAt(3) { it is NetworkOperationResult.Success }
            .assertValueAt(4) { it is NetworkOperationResult.InProgress }
            .assertValueAt(5) { it is NetworkOperationResult.Success }
            .assertValueAt(6) { it is NetworkOperationResult.InProgress }
            .assertValueAt(7) { it is NetworkOperationResult.Success }
            .assertValueCount(8)
            .assertNoErrors()
            .assertNotComplete()
    }

    @Test
    fun `should emit vehicles from other city when request fro other city comes after the first request`() {
        // given
        val otherCity = Cities.KRAKOW
        val otherCityDifficultiesDataSource: DifficultiesDataSource = mock {
            on { getDifficulties() } doReturn Single.just(DifficultiesState(false, emptyList()))
        }
        whenever(difficultiesDataSourceFactory.create(otherCity))
            .thenReturn(otherCityDifficultiesDataSource)
        whenever(difficultiesDataSource.getDifficulties()).thenReturn(
            Single.just(DifficultiesState(true, listOf(returnedDifficulty)))
        )
        val observer = tested.dataStream(selectedCity).test()

        // when
        val otherObserver = tested.dataStream(otherCity).test()
        testScheduler.triggerActions()

        // then
        observer
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) { it is NetworkOperationResult.Success
                    && it.data.isSupported
                    && it.data.difficultiesEntities == listOfDifficulties }
            .assertNoErrors()
            .assertNotComplete()

        otherObserver
            .assertValueAt(0) { it is NetworkOperationResult.InProgress }
            .assertValueAt(1) {
                it is NetworkOperationResult.Success
                        && !it.data.isSupported
                        && it.data.difficultiesEntities.isEmpty()
            }
            .assertNoErrors()
            .assertNotComplete()
    }
}