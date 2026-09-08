package com.shohan.khatago.core

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
private val prettyDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)
private val prettyDateWithYearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
private val fullDateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.ENGLISH)

fun today(): LocalDate = LocalDate.now()
fun now(): LocalDateTime = LocalDateTime.now()
fun LocalDate.toStorage(): String = format(dateFormatter)
fun LocalDateTime.toStorage(): String = format(dateTimeFormatter)
fun String.toLocalDate(): LocalDate = LocalDate.parse(this, dateFormatter)
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this, dateTimeFormatter)
fun LocalTime.toStorage(): String = format(timeFormatter)
fun String.toPrettyDate(): String = toLocalDate().format(prettyDateWithYearFormatter)
fun prettyDate(value: String): String = value.toLocalDate().format(prettyDateFormatter)
fun fullDate(value: String): String = value.toLocalDate().format(fullDateFormatter)

fun greetingFor(hour: Int = LocalTime.now().hour): String = when (hour) {
    in 0..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

fun relativeDayLabel(value: String): String {
    val date = value.toLocalDate()
    val today = today()
    return when {
        date == today -> "Today"
        date == today.plusDays(1) -> "Tomorrow"
        date == today.minusDays(1) -> "Yesterday"
        else -> prettyDate(value)
    }
}
