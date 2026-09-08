package com.shohan.khatago.domain

import com.shohan.khatago.data.local.PersonalDirection
import com.shohan.khatago.data.local.TransactionCashEffect
import com.shohan.khatago.data.local.TransactionType

data class InstallmentSeed(
    val number: Int,
    val dueDate: String,
    val amountMinor: Long
)

enum class PaymentStatus { UPCOMING, DUE_TODAY, PARTIALLY_PAID, PAID, OVERDUE }

data class DerivedInstallment(
    val id: Long,
    val number: Int,
    val dueDate: String,
    val scheduledAmountMinor: Long,
    val paidAmountMinor: Long,
    val remainingAmountMinor: Long,
    val status: PaymentStatus
)

data class AccountListItem(
    val id: Long,
    val type: String,
    val title: String,
    val subtitle: String,
    val totalAmountMinor: Long,
    val paidAmountMinor: Long,
    val remainingAmountMinor: Long,
    val nextDueDate: String?,
    val status: PaymentStatus,
    val progress: Float
)

data class DueItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val amountMinor: Long,
    val dueDate: String,
    val status: PaymentStatus,
    val accountType: String,
    val accountId: Long
)

data class TransactionListItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val amountMinor: Long,
    val cashEffect: TransactionCashEffect,
    val type: TransactionType,
    val statusLabel: String,
    val occurredAt: String
)

data class DashboardSnapshot(
    val totalOutstandingMinor: Long = 0,
    val shopOutstandingMinor: Long = 0,
    val loanOutstandingMinor: Long = 0,
    val emiOutstandingMinor: Long = 0,
    val personalOutstandingMinor: Long = 0,
    val overdueCount: Int = 0,
    val overdueMinor: Long = 0,
    val dueTodayItems: List<DueItem> = emptyList(),
    val upcomingItems: List<DueItem> = emptyList(),
    val todayIncomeMinor: Long = 0,
    val todayExpenseMinor: Long = 0,
    val todayPaymentsMinor: Long = 0,
    val recentTransactions: List<TransactionListItem> = emptyList(),
    val moneyOverview: List<MoneyPoint> = emptyList()
)

data class MoneyPoint(
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val paymentMinor: Long
)

data class ReportSummary(
    val label: String,
    val incomeMinor: Long,
    val expenseMinor: Long,
    val paymentMinor: Long,
    val outstandingMinor: Long,
    val debtDistribution: List<DistributionItem>,
    val monthlyOverview: List<MoneyPoint>,
    val insights: List<String>
) {
    val netCashFlowMinor: Long get() = incomeMinor - expenseMinor
}

data class DistributionItem(
    val label: String,
    val amountMinor: Long
)

data class SearchResults(
    val transactions: List<TransactionListItem> = emptyList(),
    val accounts: List<AccountListItem> = emptyList()
)

data class PersonalSummary(
    val direction: PersonalDirection,
    val totalMinor: Long,
    val settledMinor: Long,
    val remainingMinor: Long,
    val dueDate: String?
)

enum class ReportRange {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR
}
