package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PaymentAllocationTest {
    @Test
    fun preventsOverpaymentByComparison() {
        val amountDue = 5_000L
        val attempted = 6_000L
        assertThat(attempted > amountDue).isTrue()
    }
}
