package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val TITLE = "title"
private const val LINK = "link"

class WarsawDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val warsawDifficultiesInterface: WarsawDifficultiesInterface = mock()
    private val xmlDeserializer: XmlDeserializer = mock()

    private val tested = WarsawDifficultiesDataSource(warsawDifficultiesInterface, xmlDeserializer)

    @Test
    fun `should return list with difficulties when request succeeded`() {
        // given
        val difficultiesResponse = "RESP"
        whenever(warsawDifficultiesInterface.getDifficulties())
            .thenReturn(Single.just(difficultiesResponse))
        whenever(xmlDeserializer.deserialize(difficultiesResponse, WarsawDifficultyRss::class))
            .thenReturn(WarsawDifficultyRss(WarsawDifficultyChannel(listOf(Item(TITLE, LINK)))))

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(warsawDifficultiesInterface.getDifficulties())
            .thenReturn(Single.error(error))

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should return empty list when request succeeded given response is empty`() {
        // given
        val difficultiesResponse = ""
        whenever(warsawDifficultiesInterface.getDifficulties())
            .thenReturn(Single.just(difficultiesResponse))

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request succeeded given deserialization failed`() {
        // given
        val error: Exception = mock()
        val difficultiesResponse = "RESP"
        whenever(warsawDifficultiesInterface.getDifficulties())
            .thenReturn(Single.just(difficultiesResponse))
        whenever(xmlDeserializer.deserialize(difficultiesResponse, WarsawDifficultyRss::class))
            .thenThrow(error)

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should throw error when request succeeded given there are no difficulties`() {
        // given
        val difficultiesResponse = "RESP"
        whenever(warsawDifficultiesInterface.getDifficulties())
            .thenReturn(Single.just(difficultiesResponse))
        whenever(xmlDeserializer.deserialize(difficultiesResponse, WarsawDifficultyRss::class))
            .thenReturn(WarsawDifficultyRss(WarsawDifficultyChannel(emptyList())))

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }
}