package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DebtBalanceTest {
    @Test
    fun subtractsPaymentsFromOutstanding() {
        val original = 1_000_000L
        val paid = listOf(200_000L, 150_000L, 50_000L).sum()
        val remaining = original - paid

        assertThat(paid).isEqualTo(400_000L)
        assertThat(remaining).isEqualTo(600_000L)
    }
}
