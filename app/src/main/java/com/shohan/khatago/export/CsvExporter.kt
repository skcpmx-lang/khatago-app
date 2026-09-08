package com.shohan.khatago.export

import com.shohan.khatago.core.Money
import com.shohan.khatago.data.local.FinancialTransactionEntity
import java.lang.StringBuilder

object CsvExporter {
    private val headers = listOf("Date", "Time", "Type", "Category", "Description", "Amount", "Related Account", "Status", "Notes")

    fun export(transactions: List<FinancialTransactionEntity>): String {
        val builder = StringBuilder()
        builder.append(headers.joinToString(",") { escape(it) }).append('\n')
        transactions.forEach { item ->
            val date = item.occurredAt.substringBefore('T')
            val time = item.occurredAt.substringAfter('T', "")
            builder.append(
                listOf(
                    date,
                    time,
                    item.type.name,
                    item.category,
                    item.title,
                    Money.format(item.amountMinor),
                    item.relatedName,
                    item.statusLabel,
                    item.notes
                ).joinToString(",") { escape(it) }
            ).append('\n')
        }
        return builder.toString()
    }

    private fun escape(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
