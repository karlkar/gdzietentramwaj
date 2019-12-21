package com.kksionek.gdzietentramwaj.map.dataSource.warsaw

import com.kksionek.gdzietentramwaj.map.model.XmlDeserializer
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class WarsawDifficultiesDataSourceTest {

    private val warsawDifficultiesInterface: WarsawDifficultiesInterface = mock()
    private val xmlDeserializer: XmlDeserializer = mock()

    private val tested = WarsawDifficultiesDataSource(warsawDifficultiesInterface, xmlDeserializer)

    @Test
    fun `s`() {
        // given

        // when
        val observer = tested.getDifficulties().test()

        // then
    }
}