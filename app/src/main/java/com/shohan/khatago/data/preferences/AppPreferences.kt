package com.shohan.khatago.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

@Serializable
data class AppPreferencesState(
    val hasSeenOnboarding: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val appLockEnabled: Boolean = false,
    val biometricEnabled: Boolean = false,
    val pinHash: String? = null,
    val pinSalt: String? = null
)

class AppPreferences(private val context: Context) {
    private object Keys {
        val HasSeenOnboarding = booleanPreferencesKey("has_seen_onboarding")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val AppLockEnabled = booleanPreferencesKey("app_lock_enabled")
        val BiometricEnabled = booleanPreferencesKey("biometric_enabled")
        val PinHash = stringPreferencesKey("pin_hash")
        val PinSalt = stringPreferencesKey("pin_salt")
    }

    val state: Flow<AppPreferencesState> = context.dataStore.data.map { prefs: Preferences ->
        AppPreferencesState(
            hasSeenOnboarding = prefs[Keys.HasSeenOnboarding] ?: false,
            notificationsEnabled = prefs[Keys.NotificationsEnabled] ?: true,
            appLockEnabled = prefs[Keys.AppLockEnabled] ?: false,
            biometricEnabled = prefs[Keys.BiometricEnabled] ?: false,
            pinHash = prefs[Keys.PinHash],
            pinSalt = prefs[Keys.PinSalt]
        )
    }

    suspend fun setHasSeenOnboarding(value: Boolean) {
        context.dataStore.edit { it[Keys.HasSeenOnboarding] = value }
    }

    suspend fun setNotificationsEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.NotificationsEnabled] = value }
    }

    suspend fun setAppLockEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.AppLockEnabled] = value }
    }

    suspend fun setBiometricEnabled(value: Boolean) {
        context.dataStore.edit { it[Keys.BiometricEnabled] = value }
    }

    suspend fun savePin(hash: String, salt: String) {
        context.dataStore.edit {
            it[Keys.PinHash] = hash
            it[Keys.PinSalt] = salt
            it[Keys.AppLockEnabled] = true
        }
    }

    suspend fun clearPin() {
        context.dataStore.edit {
            it.remove(Keys.PinHash)
            it.remove(Keys.PinSalt)
            it[Keys.AppLockEnabled] = false
            it[Keys.BiometricEnabled] = false
        }
    }

    suspend fun applyBackup(state: AppPreferencesState) {
        context.dataStore.edit {
            it[Keys.HasSeenOnboarding] = state.hasSeenOnboarding
            it[Keys.NotificationsEnabled] = state.notificationsEnabled
            it[Keys.AppLockEnabled] = state.appLockEnabled
            it[Keys.BiometricEnabled] = state.biometricEnabled
            if (state.pinHash.isNullOrBlank()) it.remove(Keys.PinHash) else it[Keys.PinHash] = state.pinHash
            if (state.pinSalt.isNullOrBlank()) it.remove(Keys.PinSalt) else it[Keys.PinSalt] = state.pinSalt
        }
    }
}
