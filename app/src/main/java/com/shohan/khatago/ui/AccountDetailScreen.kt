package com.shohan.khatago.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shohan.khatago.core.Money
import com.shohan.khatago.data.KhataGoRepository
import com.shohan.khatago.domain.PaymentStatus
import com.shohan.khatago.ui.components.KhataEmptyState
import com.shohan.khatago.ui.components.KhataSectionHeader
import com.shohan.khatago.ui.components.KhataStatusBadge

@Composable
fun AccountDetailScreen(
    repository: KhataGoRepository,
    type: String,
    id: Long,
    onBack: () -> Unit,
    onAddPayment: (String, Long) -> Unit,
    onArchive: (String, Long) -> Unit,
    onDelete: (String, Long) -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    var showArchive by remember { mutableStateOf(false) }
    val canonicalType = if (type.startsWith("PERSONAL")) "PERSONAL" else type

    when (canonicalType) {
        "SHOP" -> {
            val detail by repository.observeShopDetail(id).collectAsStateWithLifecycle(initialValue = null)
            DetailScaffold(
                title = detail?.shop?.name ?: "Shop",
                onBack = onBack,
                onAddPayment = { onAddPayment("SHOP", id) },
                onArchive = { showArchive = true },
                onDelete = { showDelete = true }
            ) { padding ->
                val state = detail ?: return@DetailScaffold
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp + padding.calculateTopPadding(), bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { SummaryCard(total = state.totalMinor, paid = state.paidMinor, remaining = state.remainingMinor) }
                    item { KhataSectionHeader("Purchases") }
                    items(state.credits) { purchase ->
                        ElevatedCard {
                            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(purchase.credit.purchaseDate, style = MaterialTheme.typography.titleMedium)
                                    KhataStatusBadge(purchase.status)
                                }
                                purchase.items.forEach { item ->
                                    Text("${item.itemName} · ${item.quantityText} ${item.unit}".trim(), style = MaterialTheme.typography.bodyMedium)
                                }
                                Text("Remaining · ${Money.format(purchase.remainingMinor)}", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                    item { KhataSectionHeader("Payment History") }
                    if (state.payments.isEmpty()) {
                        item { KhataEmptyState("No payments yet", "Add a payment to start reducing this shop balance.") }
                    } else {
                        items(state.payments) { payment ->
                            ElevatedCard {
                                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(payment.paidAt.substring(0, 10), style = MaterialTheme.typography.titleMedium)
                                        Text(payment.method, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(Money.format(payment.amountMinor), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        "LOAN" -> {
            val detail by repository.observeLoanDetail(id).collectAsStateWithLifecycle(initialValue = null)
            DetailScaffold(
                title = detail?.loan?.name ?: "Loan",
                onBack = onBack,
                onAddPayment = { onAddPayment("LOAN", id) },
                onArchive = { showArchive = true },
                onDelete = { showDelete = true }
            ) { padding ->
                val state = detail ?: return@DetailScaffold
                LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp + padding.calculateTopPadding(), bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { SummaryCard(total = state.loan.totalPayableMinor, paid = state.totalPaidMinor, remaining = state.remainingMinor) }
                    item { KhataSectionHeader("Schedule") }
                    items(state.installments) { installment -> InstallmentCard(installment.number, installment.dueDate, installment.scheduledAmountMinor, installment.remainingAmountMinor, installment.status) }
                    item { KhataSectionHeader("Payment History") }
                    if (state.payments.isEmpty()) {
                        item { KhataEmptyState("No payments yet", "Add a payment to start paying down this loan.") }
                    } else {
                        items(state.payments) { payment ->
                            ElevatedCard {
                                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(payment.paidAt.substring(0, 10), style = MaterialTheme.typography.titleMedium)
                                        Text(payment.method, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(Money.format(payment.amountMinor), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        "EMI" -> {
            val detail by repository.observeEmiDetail(id).collectAsStateWithLifecycle(initialValue = null)
            DetailScaffold(
                title = detail?.emi?.productName ?: "EMI",
                onBack = onBack,
                onAddPayment = { onAddPayment("EMI", id) },
                onArchive = { showArchive = true },
                onDelete = { showDelete = true }
            ) { padding ->
                val state = detail ?: return@DetailScaffold
                LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp + padding.calculateTopPadding(), bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { SummaryCard(total = state.emi.totalPayableMinor, paid = state.totalPaidMinor, remaining = state.remainingMinor) }
                    item { KhataSectionHeader("Schedule") }
                    items(state.installments) { installment -> InstallmentCard(installment.number, installment.dueDate, installment.scheduledAmountMinor, installment.remainingAmountMinor, installment.status) }
                    item { KhataSectionHeader("Payment History") }
                    if (state.payments.isEmpty()) {
                        item { KhataEmptyState("No payments yet", "Add a payment to start paying down this EMI plan.") }
                    } else {
                        items(state.payments) { payment ->
                            ElevatedCard {
                                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(payment.paidAt.substring(0, 10), style = MaterialTheme.typography.titleMedium)
                                        Text(payment.method, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(Money.format(payment.amountMinor), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            val detail by repository.observePersonalDebtDetail(id).collectAsStateWithLifecycle(initialValue = null)
            DetailScaffold(
                title = detail?.person?.name ?: "Personal Debt",
                onBack = onBack,
                onAddPayment = { onAddPayment("PERSONAL", id) },
                onArchive = { showArchive = true },
                onDelete = { showDelete = true }
            ) { padding ->
                val state = detail ?: return@DetailScaffold
                LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp + padding.calculateTopPadding(), bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { SummaryCard(total = state.debt.amountMinor, paid = state.settledMinor, remaining = state.remainingMinor) }
                    item { KhataSectionHeader("Repayment History") }
                    if (state.settlements.isEmpty()) {
                        item { KhataEmptyState("No payments yet", "Add a payment to start tracking repayments.") }
                    } else {
                        items(state.settlements) { item ->
                            ElevatedCard {
                                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(item.settledAt.substring(0, 10), style = MaterialTheme.typography.titleMedium)
                                        Text(item.notes.ifBlank { "Payment" }, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Text(Money.format(item.amountMinor), style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showArchive) {
        AlertDialog(
            onDismissRequest = { showArchive = false },
            title = { Text("Archive this account?") },
            text = { Text("Archived accounts are hidden from the main list but their history stays in KhataGo.") },
            confirmButton = { Button(onClick = { onArchive(canonicalType, id); showArchive = false }) { Text("Archive") } },
            dismissButton = { OutlinedButton(onClick = { showArchive = false }) { Text("Cancel") } }
        )
    }
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete this account?") },
            text = { Text("Its linked history and payments may also be removed.") },
            confirmButton = { Button(onClick = { onDelete(canonicalType, id); showDelete = false }) { Text("Delete") } },
            dismissButton = { OutlinedButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun DetailScaffold(
    title: String,
    onBack: () -> Unit,
    onAddPayment: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } },
            actions = {
                TextButton(onClick = onArchive) { Text("Archive") }
                TextButton(onClick = onDelete) { Text("Delete") }
                TextButton(onClick = onAddPayment) { Text("Add Payment") }
            }
        )
    }, content = content)
}

@Composable
private fun SummaryCard(total: Long, paid: Long, remaining: Long) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Amount Due", style = MaterialTheme.typography.titleMedium)
            Text(Money.format(remaining), style = MaterialTheme.typography.headlineMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Total", style = MaterialTheme.typography.labelMedium); Text(Money.format(total), style = MaterialTheme.typography.bodyLarge) }
                Column { Text("Paid", style = MaterialTheme.typography.labelMedium); Text(Money.format(paid), style = MaterialTheme.typography.bodyLarge) }
                Column { Text("Remaining", style = MaterialTheme.typography.labelMedium); Text(Money.format(remaining), style = MaterialTheme.typography.bodyLarge) }
            }
            LinearProgressIndicator(progress = { if (total == 0L) 0f else (paid.toFloat() / total.toFloat()).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InstallmentCard(number: Int, dueDate: String, amount: Long, remaining: Long, status: PaymentStatus) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Installment #$number", style = MaterialTheme.typography.titleMedium)
                KhataStatusBadge(status)
            }
            Text("Due $dueDate", style = MaterialTheme.typography.bodyMedium)
            Text("Scheduled · ${Money.format(amount)}", style = MaterialTheme.typography.bodyLarge)
            Text("Remaining · ${Money.format(remaining)}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
