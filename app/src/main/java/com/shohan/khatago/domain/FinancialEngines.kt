package com.shohan.khatago.domain

import com.shohan.khatago.core.today
import com.shohan.khatago.core.toLocalDate
import com.shohan.khatago.core.toStorage
import com.shohan.khatago.data.local.Frequency
import java.time.LocalDate

object InstallmentScheduleGenerator {
    fun generate(
        totalPayableMinor: Long,
        installmentCount: Int,
        firstDueDate: LocalDate,
        frequency: Frequency,
        preferredInstallmentMinor: Long? = null
    ): List<InstallmentSeed> {
        require(totalPayableMinor > 0) { "Total payable must be positive." }
        require(installmentCount > 0) { "Installment count must be positive." }

        val baseAmount = preferredInstallmentMinor?.takeIf { it > 0L }
            ?: totalPayableMinor / installmentCount
        var allocated = 0L
        return (1..installmentCount).map { index ->
            val amount = if (index == installmentCount) {
                totalPayableMinor - allocated
            } else {
                val remainingSlots = (installmentCount - index).toLong()
                val maxForThisSlot = totalPayableMinor - allocated - remainingSlots
                minOf(baseAmount, maxForThisSlot)
            }
            allocated += amount
            InstallmentSeed(
                number = index,
                dueDate = when (frequency) {
                    Frequency.WEEKLY -> firstDueDate.plusWeeks((index - 1).toLong())
                    Frequency.MONTHLY -> firstDueDate.plusMonths((index - 1).toLong())
                }.toStorage(),
                amountMinor = amount
            )
        }
    }
}

object InstallmentStatusEngine {
    fun derive(dueDate: String, scheduledAmountMinor: Long, paidAmountMinor: Long, currentDate: LocalDate = today()): PaymentStatus {
        val due = dueDate.toLocalDate()
        return when {
            paidAmountMinor >= scheduledAmountMinor -> PaymentStatus.PAID
            paidAmountMinor > 0L && paidAmountMinor < scheduledAmountMinor && currentDate > due -> PaymentStatus.OVERDUE
            paidAmountMinor > 0L && paidAmountMinor < scheduledAmountMinor -> PaymentStatus.PARTIALLY_PAID
            currentDate == due -> PaymentStatus.DUE_TODAY
            currentDate > due -> PaymentStatus.OVERDUE
            else -> PaymentStatus.UPCOMING
        }
    }

    fun remainingAmount(scheduledAmountMinor: Long, paidAmountMinor: Long): Long =
        maxOf(0L, scheduledAmountMinor - paidAmountMinor)
}

object InsightEngine {
    fun build(
        incomeMinor: Long,
        expenseMinor: Long,
        overdueMinor: Long,
        upcomingCount: Int,
        largestBalance: DistributionItem?
    ): List<String> {
        val items = mutableListOf<String>()
        when {
            incomeMinor > 0L && incomeMinor > expenseMinor -> items += "Your income is higher than your expenses for this period."
            expenseMinor > incomeMinor -> items += "Your spending is higher than your income for this period."
        }
        if (upcomingCount > 0) items += "You have $upcomingCount payments coming up soon."
        if (overdueMinor > 0L) items += "Some payments are overdue and need attention."
        largestBalance?.takeIf { it.amountMinor > 0L }?.let {
            items += "Your largest outstanding balance is ${it.label}."
        }
        return items
    }
}
