package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.data.local.Frequency
import com.shohan.khatago.domain.InstallmentScheduleGenerator
import org.junit.Test
import java.time.LocalDate

class DueDateTest {
    @Test
    fun handlesLeapYearMonthEnd() {
        val schedule = InstallmentScheduleGenerator.generate(
            totalPayableMinor = 200000,
            installmentCount = 2,
            firstDueDate = LocalDate.of(2028, 1, 31),
            frequency = Frequency.MONTHLY,
            preferredInstallmentMinor = 100000
        )
        assertThat(schedule[1].dueDate).isEqualTo("2028-02-29")
    }
}
