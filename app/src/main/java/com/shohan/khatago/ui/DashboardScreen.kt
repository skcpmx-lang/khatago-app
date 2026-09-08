package com.shohan.khatago.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.core.Money
import com.shohan.khatago.core.greetingFor
import com.shohan.khatago.core.prettyDate
import com.shohan.khatago.core.today
import com.shohan.khatago.domain.DashboardSnapshot
import com.shohan.khatago.ui.components.*
import com.shohan.khatago.ui.theme.KhataAmber
import com.shohan.khatago.ui.theme.KhataNegative
import com.shohan.khatago.ui.theme.KhataPositive
import com.shohan.khatago.ui.theme.KhataPrimary

@Composable
fun DashboardScreen(
    name: String,
    snapshot: DashboardSnapshot,
    onSearch: () -> Unit,
    onAction: (AddSheetType) -> Unit,
    onOpenDetail: (String, Long) -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("KhataGo", style = MaterialTheme.typography.titleLarge)
                    Text("${greetingFor()}, ${name.ifBlank { "there" }}", style = MaterialTheme.typography.headlineMedium)
                    Text(prettyDate(today().toString()), style = MaterialTheme.typography.bodyMedium, color = LocalContentColor.current.copy(alpha = 0.65f))
                }
                FilledTonalIconButton(onClick = onSearch) { Icon(Icons.Outlined.Search, contentDescription = "Search") }
            }
        }
        item {
            KhataHeroCard(
                totalOutstandingMinor = snapshot.totalOutstandingMinor,
                shopMinor = snapshot.shopOutstandingMinor,
                loanMinor = snapshot.loanOutstandingMinor,
                emiMinor = snapshot.emiOutstandingMinor,
                personalMinor = snapshot.personalOutstandingMinor
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KhataMetricCard(
                    title = if (snapshot.overdueCount == 0) "You're all clear" else "Overdue",
                    value = if (snapshot.overdueCount == 0) Money.format(0) else Money.format(snapshot.overdueMinor),
                    subtitle = if (snapshot.overdueCount == 0) "No overdue payments" else "${snapshot.overdueCount} payments",
                    tone = if (snapshot.overdueCount == 0) KhataPositive else KhataNegative,
                    modifier = Modifier.weight(1f)
                )
                KhataMetricCard(
                    title = "Due Today",
                    value = Money.format(snapshot.dueTodayItems.sumOf { it.amountMinor }),
                    subtitle = if (snapshot.dueTodayItems.isEmpty()) "Nothing is due today." else "${snapshot.dueTodayItems.size} items",
                    tone = KhataAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item { KhataSectionHeader("Due Today") }
        if (snapshot.dueTodayItems.isEmpty()) {
            item { KhataEmptyState("Nothing is due today.", "You're clear for today.") }
        } else {
            items(snapshot.dueTodayItems) { due ->
                ElevatedCard(onClick = { onOpenDetail(due.accountType, due.accountId) }) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(due.title, style = MaterialTheme.typography.titleMedium)
                            Text(due.subtitle, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(Money.format(due.amountMinor), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item {
            KhataSectionHeader("Today's Snapshot")
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KhataMetricCard("Income", Money.format(snapshot.todayIncomeMinor, showPlus = true), KhataPositive, modifier = Modifier.weight(1f))
                KhataMetricCard("Expense", Money.format(snapshot.todayExpenseMinor), KhataNegative, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KhataMetricCard("Payments", Money.format(snapshot.todayPaymentsMinor), KhataPrimary, modifier = Modifier.weight(1f))
                KhataMetricCard("Due Today", Money.format(snapshot.dueTodayItems.sumOf { it.amountMinor }), KhataAmber, modifier = Modifier.weight(1f))
            }
        }
        item { KhataSectionHeader("Upcoming Payments") }
        if (snapshot.upcomingItems.isEmpty()) {
            item { KhataEmptyState("No payments due soon", "You're up to date for the next few days.") }
        } else {
            items(snapshot.upcomingItems) { due ->
                ElevatedCard(onClick = { onOpenDetail(due.accountType, due.accountId) }) {
                    Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(due.title, style = MaterialTheme.typography.titleMedium)
                            Text("${due.subtitle} · ${due.dueDate}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(Money.format(due.amountMinor), style = MaterialTheme.typography.titleMedium)
                            KhataStatusBadge(due.status)
                        }
                    }
                }
            }
        }
        item { KhataChartCard(snapshot.moneyOverview) }
        item { KhataSectionHeader("Quick Actions") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KhataActionTile("Shop Credit", "Track store purchases", Icons.Outlined.ShoppingBag, { onAction(AddSheetType.SHOP_CREDIT) }, Modifier.weight(1f))
                    KhataActionTile("Loan", "Add a new loan", Icons.Outlined.AccountBalance, { onAction(AddSheetType.LOAN) }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KhataActionTile("EMI", "Track installments", Icons.Outlined.CreditCard, { onAction(AddSheetType.EMI) }, Modifier.weight(1f))
                    KhataActionTile("Personal Debt", "Borrowed or lent", Icons.Outlined.ReceiptLong, { onAction(AddSheetType.PERSONAL_DEBT) }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    KhataActionTile("Income", "Log earnings", Icons.Outlined.AttachMoney, { onAction(AddSheetType.INCOME) }, Modifier.weight(1f))
                    KhataActionTile("Expense", "Track spending", Icons.Outlined.ReceiptLong, { onAction(AddSheetType.EXPENSE) }, Modifier.weight(1f))
                }
            }
        }
        item { KhataSectionHeader("Recent Transactions", action = "See All", onAction = onSeeAllTransactions) }
        if (snapshot.recentTransactions.isEmpty()) {
            item { KhataEmptyState("No transactions yet", "Add income or expenses to start building your history.") }
        } else {
            items(snapshot.recentTransactions) { transaction ->
                ElevatedCard {
                    Box(Modifier.padding(18.dp)) {
                        KhataTransactionRow(transaction)
                    }
                }
            }
        }
    }
}
