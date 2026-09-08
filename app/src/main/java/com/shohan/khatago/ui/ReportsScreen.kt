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
import com.shohan.khatago.core.Money
import com.shohan.khatago.domain.ReportRange
import com.shohan.khatago.domain.ReportSummary
import com.shohan.khatago.ui.components.KhataChartCard
import com.shohan.khatago.ui.components.KhataDistributionRow
import com.shohan.khatago.ui.components.KhataEmptyState
import com.shohan.khatago.ui.components.KhataMetricCard

@Composable
fun ReportsScreen(summary: ReportSummary, range: ReportRange, onRangeChange: (ReportRange) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Reports", style = MaterialTheme.typography.headlineMedium) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ReportRange.entries.forEach { item ->
                        FilterChip(selected = item == range, onClick = { onRangeChange(item) }, label = { Text(item.name.replace('_', ' ')) })
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KhataMetricCard("Income", Money.format(summary.incomeMinor, showPlus = true), MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                KhataMetricCard("Expense", Money.format(summary.expenseMinor), MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KhataMetricCard("Net Cash Flow", Money.format(summary.netCashFlowMinor, showPlus = summary.netCashFlowMinor > 0), MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                KhataMetricCard("Payments", Money.format(summary.paymentMinor), MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            }
        }
        item { KhataMetricCard("Outstanding Debt", Money.format(summary.outstandingMinor), MaterialTheme.colorScheme.onSurface) }
        item {
            ElevatedCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Debt Distribution", style = MaterialTheme.typography.titleMedium)
                    KhataDistributionRow(summary.debtDistribution)
                }
            }
        }
        item { KhataChartCard(summary.monthlyOverview) }
        item {
            ElevatedCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Insights", style = MaterialTheme.typography.titleMedium)
                    if (summary.insights.isEmpty()) {
                        Text("Add more activity to start seeing local financial insights.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        summary.insights.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
                    }
                }
            }
        }
    }
}
