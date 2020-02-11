package com.kksionek.gdzietentramwaj.map.dataSource.szczecin

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import org.junit.Rule
import org.junit.Test

class SzczecinDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val tested = SzczecinDifficultiesDataSource()

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