package com.shohan.khatago.core

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object Money {
    private const val SCALE = 2
    private val formatter = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.ENGLISH)).apply {
        roundingMode = RoundingMode.DOWN
    }

    fun parseToMinorUnits(raw: String): Long? = try {
        raw.trim()
            .replace(",", "")
            .takeIf { it.isNotBlank() }
            ?.let { BigDecimal(it).setScale(SCALE, RoundingMode.HALF_UP) }
            ?.movePointRight(SCALE)
            ?.longValueExact()
            ?.takeIf { it >= 0L }
    } catch (_: Exception) {
        null
    }

    fun fromMinorUnits(amountMinor: Long): BigDecimal = BigDecimal.valueOf(amountMinor, SCALE)

    fun format(amountMinor: Long, showPlus: Boolean = false): String {
        val absolute = formatter.format(fromMinorUnits(kotlin.math.abs(amountMinor)))
        val prefix = when {
            amountMinor < 0 -> "− "
            showPlus && amountMinor > 0 -> "+ "
            else -> ""
        }
        return "$prefix৳$absolute"
    }

    fun add(left: Long, right: Long): Long = Math.addExact(left, right)
    fun subtract(left: Long, right: Long): Long = Math.subtractExact(left, right)
}
