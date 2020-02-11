package com.kksionek.gdzietentramwaj.map.dataSource.lodz

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.Test

class LodzDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val tested = LodzDifficultiesDataSource()

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