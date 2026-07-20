package com.wordbook.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*

object DateUtils {

    fun nowMillis(): Long = System.currentTimeMillis()

    fun startOfDay(millis: Long): Long =
        Instant.ofEpochMilli(millis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    fun daysInMonth(year: Int, month: Int): Int {
        return LocalDate.of(year, month, 1).lengthOfMonth()
    }

    fun firstDayOfWeek(year: Int, month: Int): Int {
        val dayOfWeek = LocalDate.of(year, month, 1).dayOfWeek
        return (dayOfWeek.value % 7) + 1
    }

    fun monthName(year: Int, month: Int): String {
        return LocalDate.of(year, month, 1).month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    fun dayToEpoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }
}
