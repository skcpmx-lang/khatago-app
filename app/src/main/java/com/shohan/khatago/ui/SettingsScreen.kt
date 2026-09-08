package com.shohan.khatago.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shohan.khatago.data.preferences.AppPreferencesState
import com.shohan.khatago.ui.components.KhataEmptyState

@Composable
fun SettingsScreen(
    preferences: AppPreferencesState,
    onNotificationsChanged: (Boolean) -> Unit,
    onAppLockChanged: (Boolean) -> Unit,
    onBiometricChanged: (Boolean) -> Unit,
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
            }
        }
        item {
            SettingsSection("Security") {
                SettingsToggle("App Lock", preferences.appLockEnabled, onAppLockChanged)
                SettingsToggle("Biometric Unlock", preferences.biometricEnabled, onBiometricChanged)
            }
        }
        item {
            SettingsSection("About KhataGo") {
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
