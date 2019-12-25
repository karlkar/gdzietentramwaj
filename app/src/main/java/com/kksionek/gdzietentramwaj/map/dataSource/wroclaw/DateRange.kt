package com.kksionek.gdzietentramwaj.map.dataSource.wroclaw

import androidx.annotation.VisibleForTesting
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class DateRange { // TODO: Move to ABP

    abstract fun isInRange(otherDate: Date): Boolean

    data class SingleDate(val date: Date) : DateRange() {

        constructor(dateStr: String) : this(dateFormat.parse(dateStr)!!)

        override fun isInRange(otherDate: Date): Boolean =
            otherDate.before(date) || otherDate == date
    }

    data class RangeDate(val start: Date, val end: Date) : DateRange() {

        constructor(startDate: String, endDate: String) : this(
            dateFormat.parse(startDate)!!,
            dateFormat.parse(endDate)!!
        )

        override fun isInRange(otherDate: Date): Boolean =
            otherDate.before(start)
                    || ((otherDate == start || otherDate.after(start)) && (otherDate == end || otherDate.before(
                end
            )))
    }

    companion object {

        @VisibleForTesting
        val dateFormat = SimpleDateFormat(
            "dd.MM.yyyy",
            Locale.US
        )
    }
}