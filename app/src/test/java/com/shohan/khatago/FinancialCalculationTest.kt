package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.core.Money
import org.junit.Test

class FinancialCalculationTest {
    @Test
    fun parsesMinorUnitsWithoutFloatLoss() {
        assertThat(Money.parseToMinorUnits("100.50")).isEqualTo(10050L)
        assertThat(Money.parseToMinorUnits("100000000.00")).isEqualTo(10_000_000_000L)
    }

    @Test
    fun formatsMinorUnits() {
        assertThat(Money.format(4285000L)).isEqualTo("৳42,850")
    }
}
