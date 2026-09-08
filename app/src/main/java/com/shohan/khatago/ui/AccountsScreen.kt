package com.shohan.khatago.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.core.Money
import com.shohan.khatago.data.AccountsOverview
import com.shohan.khatago.ui.components.KhataAccountRow
import com.shohan.khatago.ui.components.KhataEmptyState
import com.shohan.khatago.ui.components.KhataSectionHeader

enum class AccountsTab(val label: String) { SHOP("Shop Credit"), LOAN("Loans"), EMI("EMI"), PERSONAL("Personal Debt") }

@Composable
fun AccountsScreen(overview: AccountsOverview, onOpenDetail: (String, Long) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(AccountsTab.SHOP) }
    val activeItems = when (tab) {
        AccountsTab.SHOP -> overview.shopCredits
        AccountsTab.LOAN -> overview.loans
        AccountsTab.EMI -> overview.emis
        AccountsTab.PERSONAL -> overview.personalBorrowed + overview.personalLent
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Accounts", style = MaterialTheme.typography.headlineMedium)
                Text("Total Outstanding · ${Money.format(overview.totalOutstandingMinor)}", style = MaterialTheme.typography.bodyLarge)
            }
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccountsTab.entries.forEach { entry ->
                    FilterChip(selected = tab == entry, onClick = { tab = entry }, label = { Text(entry.label) })
                }
            }
        }
        if (activeItems.isEmpty()) {
            item {
                KhataEmptyState(
                    title = when (tab) {
                        AccountsTab.SHOP -> "No shops yet"
                        AccountsTab.LOAN -> "No loans yet"
                        AccountsTab.EMI -> "No EMI plans yet"
                        AccountsTab.PERSONAL -> "No personal debt yet"
                    },
                    body = when (tab) {
                        AccountsTab.SHOP -> "Add your first shop to start tracking credit purchases."
                        AccountsTab.LOAN -> "Add a loan to track installments and balances."
                        AccountsTab.EMI -> "Add an EMI plan to see your payment schedule."
                        AccountsTab.PERSONAL -> "Track borrowed or lent money here."
                    }
                )
            }
        } else {
            items(activeItems) { item ->
                KhataAccountRow(item = item, onClick = {
                    onOpenDetail(
                        when (item.type) {
                            "PERSONAL_BORROWED", "PERSONAL_LENT" -> "PERSONAL"
                            else -> item.type
                        },
                        item.id
                    )
                })
            }
        }
    }
}
