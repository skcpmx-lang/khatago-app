package com.shohan.khatago.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shohan.khatago.core.Money
import com.shohan.khatago.core.relativeDayLabel
import com.shohan.khatago.domain.AccountListItem
import com.shohan.khatago.domain.DistributionItem
import com.shohan.khatago.domain.MoneyPoint
import com.shohan.khatago.domain.PaymentStatus
import com.shohan.khatago.domain.TransactionListItem
import com.shohan.khatago.ui.theme.*

private val cardShape = RoundedCornerShape(24.dp)

@Composable
fun KhataHeroCard(
    totalOutstandingMinor: Long,
    shopMinor: Long,
    loanMinor: Long,
    emiMinor: Long,
    personalMinor: Long,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = KhataPrimary),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Total Outstanding", style = MaterialTheme.typography.titleMedium, color = KhataPrimaryContainer)
            Text(
                Money.format(totalOutstandingMinor),
                style = MaterialTheme.typography.displaySmall,
                color = KhataSurface
            )
            if (totalOutstandingMinor == 0L) {
                Text("You're all clear", style = MaterialTheme.typography.bodyLarge, color = KhataPrimaryContainer)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BreakdownRow("Shop Credit", shopMinor)
                BreakdownRow("Loans", loanMinor)
                BreakdownRow("EMI", emiMinor)
                BreakdownRow("Personal Debt", personalMinor)
            }
        }
    }
}

@Composable
private fun BreakdownRow(label: String, amountMinor: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = KhataPrimaryContainer)
        Text(Money.format(amountMinor), style = MaterialTheme.typography.titleMedium, color = KhataSurface)
    }
}

@Composable
fun KhataMetricCard(
    title: String,
    value: String,
    tone: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = KhataSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = KhataTextSecondary)
            Text(value, style = MaterialTheme.typography.titleLarge, color = tone, fontWeight = FontWeight.SemiBold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary) }
        }
    }
}

@Composable
fun KhataSectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) { Text(action) }
        }
    }
}

@Composable
fun KhataStatusBadge(status: PaymentStatus) {
    val (background, text) = when (status) {
        PaymentStatus.OVERDUE -> KhataNegative.copy(alpha = 0.16f) to KhataNegative
        PaymentStatus.DUE_TODAY -> KhataAmber.copy(alpha = 0.16f) to KhataAmber
        PaymentStatus.PARTIALLY_PAID -> KhataBlue.copy(alpha = 0.14f) to KhataBlue
        PaymentStatus.PAID -> KhataPositive.copy(alpha = 0.16f) to KhataPositive
        PaymentStatus.UPCOMING -> KhataPrimaryContainer to KhataPrimary
    }
    Box(
        Modifier
            .clip(CircleShape)
            .background(background)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(status.toLabel(), color = text, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun KhataActionTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = KhataSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KhataPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = KhataPrimary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
            }
        }
    }
}

@Composable
fun KhataTransactionRow(item: TransactionListItem, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
            Text(relativeDayLabel(item.occurredAt.substring(0, 10)), style = MaterialTheme.typography.labelMedium, color = KhataTextSecondary)
        }
        Text(
            when (item.cashEffect) {
                com.shohan.khatago.data.local.TransactionCashEffect.IN -> Money.format(item.amountMinor, showPlus = true)
                com.shohan.khatago.data.local.TransactionCashEffect.OUT -> Money.format(-item.amountMinor)
                com.shohan.khatago.data.local.TransactionCashEffect.NEUTRAL -> Money.format(item.amountMinor)
            },
            style = MaterialTheme.typography.titleMedium,
            color = when (item.cashEffect) {
                com.shohan.khatago.data.local.TransactionCashEffect.IN -> KhataPositive
                com.shohan.khatago.data.local.TransactionCashEffect.OUT -> KhataTextPrimary
                com.shohan.khatago.data.local.TransactionCashEffect.NEUTRAL -> KhataBlue
            }
        )
    }
}

@Composable
fun KhataAccountRow(item: AccountListItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = KhataSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.subtitle, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
                }
                KhataStatusBadge(item.status)
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = KhataTextSecondary)
            }
            LinearProgressIndicator(
                progress = { item.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = KhataPrimary,
                trackColor = KhataPrimaryContainer
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Remaining", style = MaterialTheme.typography.labelMedium, color = KhataTextSecondary); Text(Money.format(item.remainingAmountMinor), style = MaterialTheme.typography.titleMedium) }
                Column(horizontalAlignment = Alignment.End) { Text("Next due", style = MaterialTheme.typography.labelMedium, color = KhataTextSecondary); Text(item.nextDueDate?.let(::relativeDayLabel) ?: "—", style = MaterialTheme.typography.titleMedium) }
            }
        }
    }
}

@Composable
fun KhataEmptyState(title: String, body: String, actionLabel: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = KhataSurfaceMuted),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction, shape = RoundedCornerShape(18.dp)) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun KhataDistributionRow(items: List<DistributionItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.label, style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
                Text(Money.format(item.amountMinor), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun KhataChartCard(points: List<MoneyPoint>, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = KhataSurface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Money Overview", style = MaterialTheme.typography.titleMedium)
            if (points.all { it.incomeMinor == 0L && it.expenseMinor == 0L && it.paymentMinor == 0L }) {
                Text("Add income or expenses to start seeing your trends.", style = MaterialTheme.typography.bodyMedium, color = KhataTextSecondary)
            } else {
                val max = points.maxOf { maxOf(it.incomeMinor, it.expenseMinor, it.paymentMinor) }.coerceAtLeast(1L).toFloat()
                Row(Modifier.fillMaxWidth().height(160.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
                    points.forEach { point ->
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
                                ChartBar(point.incomeMinor / max, KhataPositive)
                                ChartBar(point.expenseMinor / max, KhataNegative)
                                ChartBar(point.paymentMinor / max, KhataAmber)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(point.label, style = MaterialTheme.typography.labelMedium, color = KhataTextSecondary)
                        }
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(listOf("Income" to KhataPositive, "Expense" to KhataNegative, "Payments" to KhataAmber)) { item ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(item.second))
                            Text(item.first, style = MaterialTheme.typography.labelMedium, color = KhataTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ChartBar(ratio: Float, color: Color) {
    Box(Modifier.weight(1f).height((120 * ratio.coerceAtLeast(0.04f)).dp).clip(CircleShape).background(color))
}

private fun PaymentStatus.toLabel(): String = when (this) {
    PaymentStatus.UPCOMING -> "Upcoming"
    PaymentStatus.DUE_TODAY -> "Due Today"
    PaymentStatus.PARTIALLY_PAID -> "Partially Paid"
    PaymentStatus.PAID -> "Paid"
    PaymentStatus.OVERDUE -> "Overdue"
}
