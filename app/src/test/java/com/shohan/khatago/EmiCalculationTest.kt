package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.data.local.Frequency
import com.shohan.khatago.domain.InstallmentScheduleGenerator
import org.junit.Test
import java.time.LocalDate

class EmiCalculationTest {
    @Test
    fun createsWeeklyInstallments() {
        val schedule = InstallmentScheduleGenerator.generate(
            totalPayableMinor = 500000,
            installmentCount = 5,
            firstDueDate = LocalDate.of(2026, 9, 10),
            frequency = Frequency.WEEKLY,
            preferredInstallmentMinor = 100000
        )

        assertThat(schedule[1].dueDate).isEqualTo("2026-09-17")
        assertThat(schedule.last().amountMinor).isEqualTo(100000)
    }
}
