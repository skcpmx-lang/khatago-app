package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.domain.ReportSummary
import org.junit.Test

class CashFlowTest {
    @Test
    fun calculatesNetCashFlow() {
        val summary = ReportSummary(
            label = "This Month",
            incomeMinor = 5_000_000,
            expenseMinor = 3_200_000,
            paymentMinor = 0,
            outstandingMinor = 0,
            debtDistribution = emptyList(),
            monthlyOverview = emptyList(),
            insights = emptyList()
        )

        assertThat(summary.netCashFlowMinor).isEqualTo(1_800_000)
    }
}
