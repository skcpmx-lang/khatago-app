package com.shohan.khatago.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

data class PinSecret(val hash: String, val salt: String)

object PinSecurity {
    fun create(pin: String): PinSecret {
        require(pin.length >= 4) { "Use a 4-digit PIN or longer." }
        val saltBytes = ByteArray(16)
        SecureRandom().nextBytes(saltBytes)
        val salt = Base64.getEncoder().encodeToString(saltBytes)
        return PinSecret(hash = hash(pin, salt), salt = salt)
    }

    fun verify(pin: String, hash: String, salt: String): Boolean = hash(pin, salt) == hash

    private fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val result = digest.digest("$salt:$pin".toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(result)
    }
}
