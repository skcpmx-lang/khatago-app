package com.shohan.khatago

import com.google.common.truth.Truth.assertThat
import com.shohan.khatago.data.local.FinancialTransactionEntity
import com.shohan.khatago.data.local.TransactionCashEffect
import com.shohan.khatago.data.local.TransactionType
import com.shohan.khatago.export.CsvExporter
import org.junit.Test

class CsvExporterTest {
    @Test
    fun escapesQuotesAndCommas() {
        val csv = CsvExporter.export(
            listOf(
                FinancialTransactionEntity(
                    id = 1,
                    occurredAt = "2026-09-08T10:30:00",
                    title = "Salary, Main",
                    category = "Salary",
                    amountMinor = 5000000,
                    type = TransactionType.INCOME,
                    relatedName = "Office \"A\"",
                    relatedEntityType = "INCOME",
                    relatedEntityId = 1,
                    cashEffect = TransactionCashEffect.IN,
                    statusLabel = "Recorded",
                    notes = "Monthly, cleared"
                )
            )
        )
        assertThat(csv).contains("\"Salary, Main\"")
        assertThat(csv).contains("\"Office \"\"A\"\"\"")
    }
}
