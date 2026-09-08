package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.data.local.Frequency
import com.shohan.khatago.domain.InstallmentScheduleGenerator
import org.junit.Test
import java.time.LocalDate

class LoanCalculationTest {
    @Test
    fun createsMonthlyInstallments() {
        val schedule = InstallmentScheduleGenerator.generate(
            totalPayableMinor = 1800000,
            installmentCount = 3,
            firstDueDate = LocalDate.of(2026, 1, 31),
            frequency = Frequency.MONTHLY,
            preferredInstallmentMinor = 600000
        )

        assertThat(schedule).hasSize(3)
        assertThat(schedule[1].dueDate).isEqualTo("2026-02-28")
        assertThat(schedule.sumOf { it.amountMinor }).isEqualTo(1800000)
    }
}
