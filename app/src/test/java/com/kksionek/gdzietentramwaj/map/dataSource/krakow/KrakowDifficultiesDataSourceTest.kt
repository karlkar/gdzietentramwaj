package com.kksionek.gdzietentramwaj.map.dataSource.krakow

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import java.io.IOException

private const val DIFFICULTIES_RESPONSE = """<div class="newsBar" style="display:inline;font-size: 14px;">	
	<marquee behavior="scroll" direction="left" scrollamount="2" width="955">
	<b>Komunikaty on-line:</b>
	
			<a href="/pl/import-komunikaty/news,7168,zmiany-w-komunikacji-tramwajowej-od-dnia-9-grudnia-2019-poniedzialek-przywrocenie-ruchu-tramwajowego-na-ul-westerplatte-oraz-na-ul-dietla.html">
				<span class="data-gruba">2019-12-05 09:02:40</span> - Zmiany w komunikacji TRAMWAJOWEJ od dnia 9 grudnia 2019 (poniedziałek). Przywrócenie ruchu tramwajowego na ul. Westerplatte oraz na ul. Dietla.</a> 
				
					<span>
				
				</span>&nbsp;
	</marquee>
</div>"""

class KrakowDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val krakowDifficultiesInterface: KrakowDifficultiesInterface = mock {
        on { getDifficulties() } doReturn Single.just(DIFFICULTIES_RESPONSE)
    }

    private lateinit var tested: KrakowDifficultiesDataSource

    private fun initialize() {
        tested = KrakowDifficultiesDataSource(krakowDifficultiesInterface)
    }

    @Test
    fun `should return difficulties when request is successful`() {
        // given
        initialize()

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue {
                it.isSupported
                        && it.difficultiesEntities.size == 1
                        && it.difficultiesEntities[0].iconUrl == null
                        && it.difficultiesEntities[0].link == "http://mpk.krakow.pl/pl/import-komunikaty/news,7168,zmiany-w-komunikacji-tramwajowej-od-dnia-9-grudnia-2019-poniedzialek-przywrocenie-ruchu-tramwajowego-na-ul-westerplatte-oraz-na-ul-dietla.html"
                        && it.difficultiesEntities[0].msg == "<b>2019-12-05 09:02:40</b><br/> Zmiany w komunikacji TRAMWAJOWEJ od dnia 9 grudnia 2019 (poniedziałek). Przywrócenie ruchu tramwajowego na ul. Westerplatte oraz na ul. Dietla."
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return no difficulties when request is successful given response is empty`() {
        // given
        whenever(krakowDifficultiesInterface.getDifficulties()).thenReturn(Single.just(""))
        initialize()

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue {
                it.isSupported
                        && it.difficultiesEntities.isEmpty()
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return no difficulties when request doesn't contain matching strings`() {
        // given
        whenever(krakowDifficultiesInterface.getDifficulties()).thenReturn(Single.just("dsa"))
        initialize()

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue {
                it.isSupported
                        && it.difficultiesEntities.isEmpty()
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when link not found in text`() {
        // given
        whenever(krakowDifficultiesInterface.getDifficulties()).thenReturn(Single.just("""<div class="newsBar" style="display:inline;font-size: 14px;">	
	<marquee behavior="scroll" direction="left" scrollamount="2" width="955">
	<b>Komunikaty on-line:</b>
	
			<a href="/pl/komunikaty/news,7168,zmiany-w-komunikacji-tramwajowej-od-dnia-9-grudnia-2019-poniedzialek-przywrocenie-ruchu-tramwajowego-na-ul-westerplatte-oraz-na-ul-dietla.html">
				<span class="data-gruba">2019-12-05 09:02:40</span> - Zmiany w komunikacji TRAMWAJOWEJ od dnia 9 grudnia 2019 (poniedziałek). Przywrócenie ruchu tramwajowego na ul. Westerplatte oraz na ul. Dietla.</a> 
				
					<span>
				
				</span>&nbsp;
	</marquee>
</div>"""))
        initialize()

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertError { it is IllegalArgumentException }
    }

    @Test
    fun `should return error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(krakowDifficultiesInterface.getDifficulties()).thenReturn(Single.error(error))
        initialize()

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertError { it === error }
    }
}