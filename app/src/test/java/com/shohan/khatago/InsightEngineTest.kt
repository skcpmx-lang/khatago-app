package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.domain.DistributionItem
import com.shohan.khatago.domain.InsightEngine
import org.junit.Test

class InsightEngineTest {
    @Test
    fun generatesExpectedInsights() {
        val insights = InsightEngine.build(
            incomeMinor = 100000,
            expenseMinor = 80000,
            overdueMinor = 5000,
            upcomingCount = 3,
            largestBalance = DistributionItem("Loans", 40000)
        )

        assertThat(insights).contains("You have 3 payments coming up soon.")
        assertThat(insights).contains("Your largest outstanding balance is Loans.")
    }
}
