package com.kksionek.gdzietentramwaj.map.dataSource.zielonagora

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.Test

class ZielonaGoraDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val tested = ZielonaGoraDifficultiesDataSource()

    @Test
    fun `should not support difficulties when requested`() {
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { !it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }
}