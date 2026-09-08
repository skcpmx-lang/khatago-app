package com.shohan.khatago.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.domain.TransactionListItem
import com.shohan.khatago.ui.components.KhataEmptyState
import com.shohan.khatago.ui.components.KhataTransactionRow

@Composable
fun TransactionsScreen(
    query: String,
    mode: String,
    items: List<TransactionListItem>,
    onQueryChange: (String) -> Unit,
    onModeChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Text("Transactions", style = MaterialTheme.typography.headlineMedium) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search source or note...") },
                shape = MaterialTheme.shapes.large
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("ALL" to "All", "INCOME" to "Income", "EXPENSE" to "Expense", "PAYMENTS" to "Payments").forEach {
                    FilterChip(selected = mode == it.first, onClick = { onModeChange(it.first) }, label = { Text(it.second) })
                }
            }
        }
        if (items.isEmpty()) {
            item { KhataEmptyState("No transactions yet", "Add income or expenses to start building your history.") }
        } else {
            items(items) { item ->
                ElevatedCard {
                    Box(Modifier.padding(18.dp)) { KhataTransactionRow(item) }
                }
            }
        }
    }
}
