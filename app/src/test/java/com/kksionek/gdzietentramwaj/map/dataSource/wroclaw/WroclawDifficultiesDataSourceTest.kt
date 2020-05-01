package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.RxImmediateSchedulerRule
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.map.dataSource.wroclaw.DateRange.Companion.dateFormat
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeParseException
import java.io.IOException

private const val TITLE = "title"
private const val LINK = "link"

class WroclawDifficultiesDataSourceTest {

    @Rule
    @JvmField
    val testSchedulerRule = RxImmediateSchedulerRule()

    private val difficultyFrom: String = dateFormat.format(LocalDate.now())

    private val difficultyTo: String = dateFormat.format(LocalDate.now().plusDays(10))

    private val wroclawDifficultiesInterface: WroclawDifficultiesInterface = mock {
        on { getDifficulties() } doReturn Single.just(
            """<div class="box box-blue box-border box-large"><div class="info">
                    <span class="timestamp">16.12.2019</span>
                    <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                    <div class="plain-text">
                        <div>Dotyczy linii: Wszystkie</div>
                        <div>Obowiązuje: <span class="date-display-range">Od <span class="date-display-start">$difficultyFrom</span> do <span class="date-display-end">$difficultyTo</span></span></div>
                    </div>
                    <p class="teaser">Informujemy, że w okresie Świąt Bożego Narodzenia i Nowego Roku komunikacja tramwajowa i autobusowa będzie kursowała w następujący sposób: </p>
                    <a href="/content/108031-organizacja-komunikacji-zbiorowej-w-okresie-swiat-bozego-narodzenia-nowego-roku"><span class="read-more">Zobacz więcej ›</span></a>
                </div>
            </div>"""
        )
    }
    private val crashReportingService: CrashReportingService = mock()

    private val tested =
        WroclawDifficultiesDataSource(wroclawDifficultiesInterface, crashReportingService)

    @Test
    fun `should return difficulties when request succeeded given validity is a range`() {
        // given

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue {
                it.isSupported
                        && it.difficultiesEntities[0].iconUrl == null
                        && it.difficultiesEntities[0].msg == TITLE
                        && it.difficultiesEntities[0].link == WROCLAW_BASE_URL + LINK

            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request failed`() {
        // given
        val error: IOException = mock()
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(Single.error(error))

        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertError { it === error }
    }

    @Test
    fun `should not return difficulties when request succeeded given difficulties are from different period`() {
        // given
        val difficultyFrom = "10.10.2000"
        val difficultyTo = "10.12.2000"
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large"><div class="info">
                    <span class="timestamp">16.12.2019</span>
                    <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                    <div class="plain-text">
                        <div>Dotyczy linii: Wszystkie</div>
                        <div>Obowiązuje: <span class="date-display-range">Od <span class="date-display-start">$difficultyFrom</span> do <span class="date-display-end">$difficultyTo</span></span></div>
                    </div>
                    <p class="teaser">Informujemy, że w okresie Świąt Bożego Narodzenia i Nowego Roku komunikacja tramwajowa i autobusowa będzie kursowała w następujący sposób: </p>
                    <a href="/content/108031-organizacja-komunikacji-zbiorowej-w-okresie-swiat-bozego-narodzenia-nowego-roku"><span class="read-more">Zobacz więcej ›</span></a>
                </div>
            </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return difficulties when request succeeded given validity is a single date`() {
        // given
        val currentDate = dateFormat.format(LocalDate.now())
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                            <div>Obowiązuje: <span class="date-display-single">$currentDate</span></div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer
            .assertValue {
                it.isSupported
                        && it.difficultiesEntities[0].iconUrl == null
                        && it.difficultiesEntities[0].msg == TITLE
                        && it.difficultiesEntities[0].link == WROCLAW_BASE_URL + LINK
            }
            .assertNoErrors()
            .assertComplete()
    }

    @Test
    fun `should return error when request succeeded given validity is a single date and format is invalid`() {
        // given
        val currentDate = dateFormat.format(LocalDate.now())
        val currentDateWithWrongFormat = currentDate.replace('.', '/')
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                            <div>Obowiązuje: <span class="date-display-single">$currentDateWithWrongFormat</span></div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertError(DateTimeParseException::class.java) // TODO: Should be reported to crashlytics, maybe just 1 item should be skipped?
    }

    @Test
    fun `should return error when request succeeded given validity is a date range and format is invalid`() {
        // given
        val difficultyFromWrongFormat = difficultyFrom.replace('.', '/')
        val difficultyToWrongFormat = difficultyTo.replace('.', '/')
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                            <div>Obowiązuje: <span class="date-display-range">Od <span class="date-display-start">$difficultyFromWrongFormat</span> do <span class="date-display-end">$difficultyToWrongFormat</span></span></div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertError(DateTimeParseException::class.java) // TODO: Should be reported to crashlytics, maybe just 1 item should be skipped like in case when link/date are missing
    }

    @Test
    fun `should return no difficulties when request succeeded given validity is missing`() {
        // given
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
    }

    @Test
    fun `should report to crashlytics when request succeeded given validity is missing`() {
        // given
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <h3 class="title"><a href="$LINK">$TITLE</a></h3>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        tested.getDifficulties().test()

        // then
        verify(crashReportingService).reportCrash(
            argThat { this is IllegalArgumentException },
            eq(null)
        )
    }

    @Test
    fun `should return no difficulties when request succeeded given link is missing`() {
        // given
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                            <div>Obowiązuje: <span class="date-display-range">Od <span class="date-display-start">$difficultyFrom</span> do <span class="date-display-end">$difficultyTo</span></span></div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        val observer = tested.getDifficulties().test()

        // then
        observer.assertValue { it.isSupported && it.difficultiesEntities.isEmpty() }
    }

    @Test
    fun `should report to crashlytics when request succeeded given link is missing`() {
        // given
        whenever(wroclawDifficultiesInterface.getDifficulties()).thenReturn(
            Single.just(
                """<div class="box box-blue box-border box-large">
                    <div class="info">
                        <span class="timestamp">16.12.2019</span>
                        <div class="plain-text">
                            <div>Dotyczy linii: 4, 15, 127</div>
                            <div>Obowiązuje: <span class="date-display-range">Od <span class="date-display-start">$difficultyFrom</span> do <span class="date-display-end">$difficultyTo</span></span></div>
                        </div>
                        <p class="teaser">W związku z planowanym zakończeniem prac związanych z przebudową rozjazdów na skrzyżowaniu ulicy Sądowej i Podwale, od 21 grudnia 2019 roku (sobota) planowane jest wprowadzenie zmiany na linii tramwajowej 4 i 15, które powrócą na swoje stałe trasy przejazdu. Wraz z udrożnieniem ulicy Sądowej zostanie wprowadzona zmiana trasy linii autobusowej 127.</p>
                        <a href="/content/108028-21122019r-zakonczenie-przebudowy-rozjazdow-na-skrzyzowaniu-ulicy-sadowej-i-podwale"><span class="read-more">Zobacz więcej ›</span></a>
                    </div>
                </div>"""
            )
        )
        // when
        tested.getDifficulties().test()

        // then
        verify(crashReportingService).reportCrash(
            argThat { this is IllegalArgumentException },
            eq(null)
        )
    }
}