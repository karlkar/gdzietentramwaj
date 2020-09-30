package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import androidx.annotation.VisibleForTesting
import java.time.LocalDate
import java.time.format.DateTimeFormatter

sealed class DateRange {

    abstract fun isInRange(otherDate: LocalDate): Boolean

    data class SingleDate(val date: LocalDate) : DateRange() {

        constructor(dateStr: String) : this(LocalDate.parse(dateStr, dateFormat))

        override fun isInRange(otherDate: LocalDate): Boolean = otherDate <= date
    }

    data class RangeDate(val start: LocalDate, val end: LocalDate) : DateRange() {

        constructor(startDate: String, endDate: String) : this(
            LocalDate.parse(startDate, dateFormat),
            LocalDate.parse(endDate, dateFormat)
        )

        override fun isInRange(otherDate: LocalDate): Boolean = otherDate in start..end
    }

    companion object {

        @VisibleForTesting
        val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}