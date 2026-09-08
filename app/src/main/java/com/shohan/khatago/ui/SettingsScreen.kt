package com.shohan.khatago.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.shohan.khatago.R
import com.shohan.khatago.data.preferences.AppPreferencesState

@Composable
fun SettingsScreen(
    preferences: AppPreferencesState,
    hasPin: Boolean,
    onNotificationsChanged: (Boolean) -> Unit,
    onAppLockChanged: (Boolean) -> Unit,
    onBiometricChanged: (Boolean) -> Unit,
    onSetPin: () -> Unit,
    onClearPin: () -> Unit,
    onCreateBackup: () -> Unit,
    onRestoreBackup: () -> Unit,
    onExportCsv: () -> Unit,
    onExportPdf: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Text("Settings", style = MaterialTheme.typography.headlineMedium) }
        item {
            SettingsSection("Data & Backup") {
                Text("Backup files may contain sensitive financial data. Keep them private.", style = MaterialTheme.typography.bodyMedium)
                SettingsButton("Create Backup", onCreateBackup)
                SettingsButton("Restore Backup", onRestoreBackup)
            }
        }
        item {
            SettingsSection("Reports & Export") {
                SettingsButton("Export CSV", onExportCsv)
                SettingsButton("Export PDF", onExportPdf)
            }
        }
        item {
            SettingsSection("Notifications") {
                SettingsToggle("Payment Reminders", preferences.notificationsEnabled, onNotificationsChanged)
                Text("KhataGo only reminds you about upcoming, due today and overdue payments.", style = MaterialTheme.typography.bodyMedium)
            }
        }
        item {
            SettingsSection("Security") {
                SettingsToggle("App Lock", preferences.appLockEnabled, onAppLockChanged)
                SettingsToggle("Biometric Unlock", preferences.biometricEnabled, onBiometricChanged)
                SettingsButton(if (hasPin) "Change PIN" else "Set PIN", onSetPin)
                if (hasPin) {
                    OutlinedButton(onClick = onClearPin, modifier = Modifier.fillMaxWidth()) { Text("Remove PIN") }
                }
            }
        }
        item {
            SettingsSection("About KhataGo") {
                Image(
                    painter = painterResource(R.drawable.ic_khatago_mark),
                    contentDescription = "KhataGo mark",
                    modifier = Modifier.size(56.dp)
                )
                Text("KhataGo", style = MaterialTheme.typography.titleMedium)
                Text("All your finances, in one place.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                Text("Created by Shohan Khan", style = MaterialTheme.typography.bodyMedium)
                Text("helloiamshohan@gmail.com", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun SettingsButton(title: String, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(title) }
}

@Composable
private fun SettingsToggle(title: String, value: Boolean, onChanged: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = value, onCheckedChange = onChanged)
    }
}
