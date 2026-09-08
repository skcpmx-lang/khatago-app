package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.domain.InstallmentStatusEngine
import com.shohan.khatago.domain.PaymentStatus
import org.junit.Test
import java.time.LocalDate

class InstallmentStatusTest {
    @Test
    fun marksPartiallyPaidInstallment() {
        val status = InstallmentStatusEngine.derive(
            dueDate = "2026-09-08",
            scheduledAmountMinor = 500000,
            paidAmountMinor = 200000,
            currentDate = LocalDate.of(2026, 9, 8)
        )
        assertThat(status).isEqualTo(PaymentStatus.PARTIALLY_PAID)
    }

    @Test
    fun marksPaidInstallment() {
        val status = InstallmentStatusEngine.derive(
            dueDate = "2026-09-08",
            scheduledAmountMinor = 500000,
            paidAmountMinor = 500000,
            currentDate = LocalDate.of(2026, 9, 10)
        )
        assertThat(status).isEqualTo(PaymentStatus.PAID)
    }
}
