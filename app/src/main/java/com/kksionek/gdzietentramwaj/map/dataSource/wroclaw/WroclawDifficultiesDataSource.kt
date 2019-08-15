package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import com.kksionek.gdzietentramwaj.base.crash.CrashReportingService
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesDataSource
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesEntity
import com.kksionek.gdzietentramwaj.map.dataSource.DifficultiesState
import io.reactivex.Single
import java.util.Calendar
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND

class WroclawDifficultiesDataSource(
    private val wroclawDifficultiesInterface: WroclawDifficultiesInterface,
    private val crashReportingService: CrashReportingService
) : DifficultiesDataSource {

    override fun getDifficulties(): Single<DifficultiesState> {
        val cal = Calendar.getInstance().apply {
            set(HOUR_OF_DAY, 0)
            set(MINUTE, 0)
            set(SECOND, 0)
            set(MILLISECOND, 0)
        }
        val today = cal.time
        return wroclawDifficultiesInterface.getDifficulties()
            .map external@{ result ->
                val difficultiesList = pattern.findAll(result)
                    .map { matchResult ->
                        val range: DateRange
                        val singleDate =
                            dateSinglePattern.find(matchResult.groupValues[1])?.groupValues?.get(1)
                        if (singleDate == null) {
                            val rangeResult = dateRangePattern.find(matchResult.groupValues[1])
                            val startDate = rangeResult?.groupValues?.get(1)
                            if (startDate == null) {
                                crashReportingService.reportCrash(IllegalArgumentException("[WROCLAW][DIFFICULTIES] No date range detected in '${matchResult.groupValues[1]}'"))
                                return@map null
                            }
                            val endDate = rangeResult.groupValues[2]
                            range = DateRange.RangeDate(startDate, endDate)
                        } else {
                            range = DateRange.SingleDate(singleDate)
                        }
                        val titleAndLinkResult =
                            titleAndLinkPattern.find(matchResult.groupValues[1])
                        val link = titleAndLinkResult?.groupValues?.get(1)
                        if (link == null) {
                            crashReportingService.reportCrash(IllegalArgumentException("[WROCLAW][DIFFICULTIES] No title/link detected in '${matchResult.groupValues[1]}'"))
                            return@map null
                        }
                        val title = titleAndLinkResult.groupValues[2]
                        range to DifficultiesEntity(null, title, "http://mpk.wroc.pl$link")
                    }
                    .filterNotNull()
                    .takeWhile { it.first.isInRange(today) }
                    .map { it.second }
                    .toList()
                DifficultiesState(true, difficultiesList)
            }
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