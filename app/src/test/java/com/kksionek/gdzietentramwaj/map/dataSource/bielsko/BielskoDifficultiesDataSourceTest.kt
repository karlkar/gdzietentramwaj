package com.kksionek.gdzietentramwaj.map.dataSource.bielsko

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.Test

class BielskoDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val tested = BielskoDifficultiesDataSource()

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