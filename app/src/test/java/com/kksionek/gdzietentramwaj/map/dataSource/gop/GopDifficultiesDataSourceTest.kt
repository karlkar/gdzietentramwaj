package com.kksionek.gdzietentramwaj.map.dataSource.gop

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.Test

class GopDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val tested = GopDifficultiesDataSource()

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