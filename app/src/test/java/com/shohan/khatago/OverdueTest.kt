package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.domain.InstallmentStatusEngine
import com.shohan.khatago.domain.PaymentStatus
import org.junit.Test
import java.time.LocalDate

class OverdueTest {
    @Test
    fun marksUnpaidPastDueAsOverdue() {
        val status = InstallmentStatusEngine.derive(
            dueDate = "2026-09-01",
            scheduledAmountMinor = 500000,
            paidAmountMinor = 0,
            currentDate = LocalDate.of(2026, 9, 8)
        )
        assertThat(status).isEqualTo(PaymentStatus.OVERDUE)
    }
}
