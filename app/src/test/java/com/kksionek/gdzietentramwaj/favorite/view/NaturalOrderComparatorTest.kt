package com.kksionek.gdzietentramwaj.favorite.view

import org.amshove.kluent.`should be`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class NaturalOrderComparatorTest(
    private val param1: String,
    private val param2: String,
    private val expected: Int
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("100", "1", 1),
                arrayOf("200", "3", 1),
                arrayOf("2", "21", -1),
                arrayOf("10", "10", 0),
                arrayOf("", "", 0),
                arrayOf("", "1", -1),
                arrayOf(" 1", "1", 0),
                arrayOf(" 1", "10", -1)
            )
        }
    }

    private val tested = NaturalOrderComparator<String>()

    @Test
    fun `should properly sort strings with numbers in natural order`() {
        // when
        val result = tested.compare(param1, param2)

        // then
        result `should be` expected
    }
}