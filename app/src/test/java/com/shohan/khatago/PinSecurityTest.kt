package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.security.PinSecurity
import org.junit.Test

class PinSecurityTest {
    @Test
    fun hashesAndVerifiesPin() {
        val secret = PinSecurity.create("1234")
        assertThat(PinSecurity.verify("1234", secret.hash, secret.salt)).isTrue()
        assertThat(PinSecurity.verify("9999", secret.hash, secret.salt)).isFalse()
    }
}
