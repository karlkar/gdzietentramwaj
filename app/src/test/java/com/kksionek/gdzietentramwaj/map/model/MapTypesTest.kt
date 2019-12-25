package com.kksionek.gdzietentramwaj.map.model

import org.amshove.kluent.`should be`
import org.junit.Test

class MapTypesTest {

    @Test
    fun `should return first item when next is requested so many times as MapTypes values count`() {
        // given
        val initial = MapTypes.NORMAL

        // when
        val result = initial.next().next().next().next()

        // then
        result `should be` initial
    }
}