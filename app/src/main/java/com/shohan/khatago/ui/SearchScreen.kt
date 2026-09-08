package com.shohan.khatago.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.domain.SearchResults
import com.shohan.khatago.ui.components.KhataAccountRow
import com.shohan.khatago.ui.components.KhataEmptyState
import com.shohan.khatago.ui.components.KhataSectionHeader
import com.shohan.khatago.ui.components.KhataTransactionRow

@Composable
fun SearchScreen(
    query: String,
    results: SearchResults,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenDetail: (String, Long) -> Unit
) {
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Search") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Back") } }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp + padding.calculateTopPadding(), bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Search transactions, accounts or notes") },
                    shape = MaterialTheme.shapes.large
                )
            }
            if (results.accounts.isEmpty() && results.transactions.isEmpty()) {
                item { KhataEmptyState("Nothing found", "Try a shop name, institution, person or note.") }
            } else {
                item { KhataSectionHeader("Accounts") }
                items(results.accounts) { item ->
                    KhataAccountRow(item = item, onClick = {
                        onOpenDetail(if (item.type.startsWith("PERSONAL")) "PERSONAL" else item.type, item.id)
                    })
                }
                item { KhataSectionHeader("Transactions") }
                items(results.transactions) { item ->
                    ElevatedCard { Box(Modifier.padding(18.dp)) { KhataTransactionRow(item) } }
                }
            }
        }
    }
}
