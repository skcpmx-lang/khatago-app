package com.shohan.khatago.core

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Error(val message: String) : ValidationResult
}

fun validateRequired(vararg values: String): ValidationResult =
    if (values.all { it.isNotBlank() }) ValidationResult.Valid
    else ValidationResult.Error("Please complete the required fields.")

fun validateAmount(amountMinor: Long?): ValidationResult = when {
    amountMinor == null -> ValidationResult.Error("Enter a valid amount.")
    amountMinor <= 0L -> ValidationResult.Error("Enter a valid amount.")
    else -> ValidationResult.Valid
}
