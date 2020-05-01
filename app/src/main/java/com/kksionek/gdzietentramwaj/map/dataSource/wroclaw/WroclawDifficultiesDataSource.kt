package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import androidx.annotation.VisibleForTesting
import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.model.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.model.DifficultiesState
import io.reactivex.Single
import org.threeten.bp.LocalDate

@VisibleForTesting
const val WROCLAW_BASE_URL = "http://mpk.wroc.pl"

class WroclawDifficultiesDataSource(
    private val wroclawDifficultiesInterface: WroclawDifficultiesInterface,
    private val crashReportingService: CrashReportingService
) : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> {
        return wroclawDifficultiesInterface.getDifficulties()
            .map external@{ result ->
                val difficultiesList = parseDifficultiesList(result)
                DifficultiesState(true, difficultiesList)
            }
    }

    private fun parseDifficultiesList(result: String): List<DifficultiesEntity> {
        val today = getCurrentDate()
        return pattern.findAll(result)
            .map { matchResult ->
                val range = getDateRange(matchResult)
                if (range == null) {
                    crashReportingService.reportCrash(IllegalArgumentException("[WROCLAW][DIFFICULTIES] No date range detected in '${matchResult.groupValues[1]}'"))
                    return@map null
                }
                val titleAndLinkResult = titleAndLinkPattern.find(matchResult.groupValues[1])
                val link = titleAndLinkResult?.groupValues?.get(1)
                if (link == null) {
                    crashReportingService.reportCrash(IllegalArgumentException("[WROCLAW][DIFFICULTIES] No title/link detected in '${matchResult.groupValues[1]}'"))
                    return@map null
                }
                val title = titleAndLinkResult.groupValues[2]
                range to DifficultiesEntity(
                    null,
                    title,
                    WROCLAW_BASE_URL + link
                )
            }
            .filterNotNull()
            .takeWhile { it.first.isInRange(today) }
            .map { it.second }
            .toList()
    }

    private fun getCurrentDate(): LocalDate = LocalDate.now()

    private fun getDateRange(matchResult: MatchResult): DateRange? {
        val singleDate = dateSinglePattern.find(matchResult.groupValues[1])?.groupValues?.get(1)
        return singleDate?.let { DateRange.SingleDate(it) } ?: parseRangeDate(matchResult)
    }

    private fun parseRangeDate(matchResult: MatchResult): DateRange? {
        val rangeResult = dateRangePattern.find(matchResult.groupValues[1])
        val startDate = rangeResult?.groupValues?.get(1) ?: return null
        val endDate = rangeResult.groupValues[2]
        return DateRange.RangeDate(startDate, endDate)
    }

    companion object {
        private val pattern =
            "<div class=\"box box-blue box-border box-large\">(.*?)<\\/div>\\s+<\\/div>".toRegex(
                RegexOption.DOT_MATCHES_ALL
            )
        private val dateSinglePattern =
            "<span class=\"date-display-single\">(.*?)<\\/span>".toRegex()
        private val dateRangePattern =
            "<span class=\"date-display-range\">.*?<span class=\"date-display-start\">(.*?)<\\/span>.*?<span class=\"date-display-end\">(.*?)<\\/span><\\/span>".toRegex()
        private val titleAndLinkPattern =
            "<h3 class=\"title\"><a href=\"(.*?)\">(.*?)<\\/a><\\/h3>".toRegex(RegexOption.DOT_MATCHES_ALL)
    }
}