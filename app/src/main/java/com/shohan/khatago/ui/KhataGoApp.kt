package com.shohan.khatago.ui

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.work.WorkManager
import com.shohan.khatago.AppContainer
import com.shohan.khatago.data.AccountsOverview
import com.shohan.khatago.data.BackupPayload
import com.shohan.khatago.data.preferences.AppPreferencesState
import com.shohan.khatago.domain.DashboardSnapshot
import com.shohan.khatago.domain.ReportRange
import com.shohan.khatago.domain.ReportSummary
import com.shohan.khatago.export.PdfExporter
import com.shohan.khatago.notifications.ReminderScheduler
import com.shohan.khatago.security.BiometricHelper
import com.shohan.khatago.security.PinSecurity
import kotlinx.coroutines.launch

@Composable
fun KhataGoApp(container: AppContainer) {
    val navController = rememberNavController()
    val repository = container.repository
    val preferences by container.preferences.state.collectAsStateWithLifecycle(initialValue = AppPreferencesState())
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    var lockError by remember { mutableStateOf<String?>(null) }
    var isLocked by remember(preferences.appLockEnabled, preferences.pinHash, preferences.pinSalt) {
        mutableStateOf(preferences.appLockEnabled && !preferences.pinHash.isNullOrBlank() && !preferences.pinSalt.isNullOrBlank())
    }

    LaunchedEffect(preferences.hasSeenOnboarding, profile?.name) {
        val target = when {
            !preferences.hasSeenOnboarding -> Routes.ONBOARDING
            profile?.name.isNullOrBlank() -> Routes.SETUP
            else -> Routes.HOME
        }
        if (navController.currentBackStackEntry?.destination?.route != target) {
            navController.navigate(target) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    DisposableEffect(lifecycleOwner, preferences.appLockEnabled, preferences.pinHash, preferences.pinSalt) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP && preferences.appLockEnabled && !preferences.pinHash.isNullOrBlank() && !preferences.pinSalt.isNullOrBlank()) {
                isLocked = true
                lockError = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
        Box(Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = Routes.ONBOARDING, modifier = Modifier.fillMaxSize()) {
                composable(Routes.ONBOARDING) {
                    OnboardingScreen(
                        onSkip = {
                            scope.launch { container.preferences.setHasSeenOnboarding(true) }
                            navController.navigate(Routes.SETUP) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                        },
                        onContinue = {
                            scope.launch { container.preferences.setHasSeenOnboarding(true) }
                            navController.navigate(Routes.SETUP) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                        }
                    )
                }
                composable(Routes.SETUP) {
                    var name by rememberSaveable { mutableStateOf(profile?.name.orEmpty()) }
                    var error by remember { mutableStateOf<String?>(null) }
                    SetupScreen(
                        name = name,
                        onNameChange = { name = it; error = null },
                        onContinue = {
                            if (name.isBlank()) {
                                error = "Please complete the required fields."
                            } else {
                                scope.launch {
                                    repository.saveUserProfile(name)
                                    snackbarHostState.showSnackbar("Welcome, $name")
                                }
                            }
                        },
                        error = error
                    )
                }
                composable(Routes.HOME) {
                    HomeRoute(
                        container = container,
                        preferences = preferences,
                        profileName = profile?.name.orEmpty(),
                        onSearch = { navController.navigate(Routes.SEARCH) },
                        onOpenDetail = { type, id -> navController.navigate(Routes.detail(type, id)) },
                        onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
                    )
                }
                composable(Routes.SEARCH) {
                    var query by rememberSaveable { mutableStateOf("") }
                    val results by repository.observeSearch(query).collectAsStateWithLifecycle(initialValue = com.shohan.khatago.domain.SearchResults())
                    SearchScreen(
                        query = query,
                        results = results,
                        onQueryChange = { query = it },
                        onBack = { navController.popBackStack() },
                        onOpenDetail = { type, id -> navController.navigate(Routes.detail(type, id)) }
                    )
                }
                composable(
                    route = "${Routes.DETAIL}/{type}/{id}",
                    arguments = listOf(
                        navArgument("type") { type = NavType.StringType },
                        navArgument("id") { type = NavType.LongType }
                    )
                ) { backStackEntry ->
                    val type = backStackEntry.arguments?.getString("type").orEmpty()
                    val id = backStackEntry.arguments?.getLong("id") ?: 0L
                    var paymentTarget by remember { mutableStateOf<Pair<String, Long>?>(null) }
                    var activeSheet by remember { mutableStateOf<AddSheetType?>(null) }
                    val categories by repository.customCategories.collectAsStateWithLifecycle(initialValue = emptyList())
                    AccountDetailScreen(
                        repository = repository,
                        type = type,
                        id = id,
                        onBack = { navController.popBackStack() },
                        onAddPayment = { paymentType, paymentId ->
                            paymentTarget = paymentType to paymentId
                            activeSheet = AddSheetType.PAYMENT
                        },
                        onArchive = { accountType, accountId ->
                            scope.launch {
                                repository.archiveAccount(accountType, accountId)
                                snackbarHostState.showSnackbar("Account archived.")
                                navController.popBackStack()
                            }
                        },
                        onDelete = { accountType, accountId ->
                            scope.launch {
                                repository.deleteAccount(accountType, accountId)
                                snackbarHostState.showSnackbar("Account deleted.")
                                navController.popBackStack()
                            }
                        }
                    )
                    activeSheet?.let { sheet ->
                        AddEntrySheet(
                            type = sheet,
                            categories = categories,
                            paymentTarget = paymentTarget,
                            onDismiss = { activeSheet = null },
                            onSaveShopCredit = repository::addShopCredit,
                            onSaveLoan = repository::addLoan,
                            onSaveEmi = repository::addEmi,
                            onSavePersonalDebt = repository::addPersonalDebt,
                            onSaveIncome = repository::addIncome,
                            onSaveExpense = repository::addExpense,
                            onSavePayment = repository::addPayment,
                            onMessage = { message -> scope.launch { snackbarHostState.showSnackbar(message) } }
                        )
                    }
                }
            }

            if (isLocked && preferences.appLockEnabled && !preferences.pinHash.isNullOrBlank() && !preferences.pinSalt.isNullOrBlank()) {
                AppLockScreen(
                    biometricEnabled = preferences.biometricEnabled,
                    error = lockError,
                    onPinSubmit = { pin ->
                        val ok = PinSecurity.verify(pin, preferences.pinHash.orEmpty(), preferences.pinSalt.orEmpty())
                        if (ok) {
                            isLocked = false
                            lockError = null
                        } else {
                            lockError = "That PIN didn't match."
                        }
                    },
                    onBiometric = activity?.let { currentActivity ->
                        {
                            val helper = BiometricHelper(currentActivity)
                            if (helper.canAuthenticate()) {
                                helper.authenticate(
                                    onSuccess = { isLocked = false; lockError = null },
                                    onError = { message -> lockError = message }
                                )
                            } else {
                                lockError = "Biometric unlock isn't available on this device."
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeRoute(
    container: AppContainer,
    preferences: AppPreferencesState,
    profileName: String,
    onSearch: () -> Unit,
    onOpenDetail: (String, Long) -> Unit,
    onMessage: (String) -> Unit
) {
    val repository = container.repository
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val dashboard by repository.observeDashboard().collectAsStateWithLifecycle(initialValue = DashboardSnapshot())
    val accounts by repository.observeAccountsOverview().collectAsStateWithLifecycle(
        initialValue = AccountsOverview(0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )
    val categories by repository.customCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }
    var activeSheet by remember { mutableStateOf<AddSheetType?>(null) }
    var paymentTarget by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var transactionQuery by rememberSaveable { mutableStateOf("") }
    var transactionMode by rememberSaveable { mutableStateOf("ALL") }
    var reportRange by rememberSaveable { mutableStateOf(ReportRange.THIS_MONTH) }
    var pendingPinSetup by remember { mutableStateOf(false) }
    var pendingRestoreContent by remember { mutableStateOf<String?>(null) }
    var pendingRestorePayload by remember { mutableStateOf<BackupPayload?>(null) }

    val reportSummary by repository.observeReports(reportRange).collectAsStateWithLifecycle(
        initialValue = ReportSummary("This Month", 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
    )

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) {
            ReminderScheduler.ensureScheduled(context)
            scope.launch { container.preferences.setNotificationsEnabled(true) }
            onMessage("Payment reminders are on.")
        } else {
            scope.launch { container.preferences.setNotificationsEnabled(false) }
            onMessage("Notification permission was not granted.")
        }
    }

    val backupLauncher = rememberLauncherForActivityResult(CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val json = repository.exportBackup(preferences)
                    context.writeTextToUri(it, json)
                }.onSuccess {
                    onMessage("Backup created.")
                }.onFailure {
                    onMessage("Backup couldn't be created.")
                }
            }
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(CreateDocument("text/csv")) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val csv = repository.exportTransactionsCsv()
                    context.writeTextToUri(it, csv)
                }.onSuccess {
                    onMessage("Report exported.")
                }.onFailure {
                    onMessage("CSV export couldn't be created.")
                }
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(CreateDocument("application/pdf")) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    PdfExporter.export(context, it, reportSummary.label, reportSummary)
                }.onSuccess {
                    onMessage("Report exported.")
                }.onFailure {
                    onMessage("PDF export couldn't be created.")
                }
            }
        }
    }

    val restoreLauncher = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                runCatching {
                    val content = context.readTextFromUri(it)
                    val payload = repository.parseBackup(content)
                    repository.validateBackup(payload)
                    pendingRestoreContent = content
                    pendingRestorePayload = payload
                }.onFailure {
                    onMessage(it.message ?: "This backup file can't be read.")
                }
            }
        }
    }

    LaunchedEffect(preferences.notificationsEnabled) {
        if (preferences.notificationsEnabled) {
            ReminderScheduler.createChannel(context)
        } else {
            WorkManager.getInstance(context).cancelUniqueWork("payment_reminder_worker")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item,
                        onClick = { selectedTab = item },
                        icon = {
                            Icon(
                                when (item) {
                                    MainTab.DASHBOARD -> Icons.Outlined.Dashboard
                                    MainTab.ACCOUNTS -> Icons.Outlined.Wallet
                                    MainTab.TRANSACTIONS -> Icons.Outlined.ReceiptLong
                                    MainTab.REPORTS -> Icons.Outlined.Assessment
                                    MainTab.SETTINGS -> Icons.Outlined.Settings
                                },
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            var expanded by remember { mutableStateOf(false) }
            Box {
                FloatingActionButton(onClick = { expanded = true }, modifier = Modifier.navigationBarsPadding()) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf(
                        "Add Shop Credit" to AddSheetType.SHOP_CREDIT,
                        "Add Loan" to AddSheetType.LOAN,
                        "Add EMI" to AddSheetType.EMI,
                        "Add Personal Debt" to AddSheetType.PERSONAL_DEBT,
                        "Add Income" to AddSheetType.INCOME,
                        "Add Expense" to AddSheetType.EXPENSE,
                        "Add Payment" to AddSheetType.PAYMENT
                    ).forEach { (label, sheet) ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { expanded = false; activeSheet = sheet })
                    }
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        name = profileName,
                        snapshot = dashboard,
                        onSearch = onSearch,
                        onAction = { sheet ->
                            activeSheet = sheet
                            if (sheet == AddSheetType.PAYMENT) paymentTarget = null
                        },
                        onOpenDetail = onOpenDetail,
                        onSeeAllTransactions = { selectedTab = MainTab.TRANSACTIONS }
                    )
                    MainTab.ACCOUNTS -> AccountsScreen(overview = accounts, onOpenDetail = onOpenDetail)
                    MainTab.TRANSACTIONS -> {
                        val txns by repository.observeTransactions(transactionQuery, transactionMode).collectAsStateWithLifecycle(initialValue = emptyList())
                        TransactionsScreen(
                            query = transactionQuery,
                            mode = transactionMode,
                            items = txns,
                            onQueryChange = { transactionQuery = it },
                            onModeChange = { transactionMode = it },
                            onDeleteIncomeExpense = { transactionId ->
                                scope.launch {
                                    runCatching { repository.deleteCashTransaction(transactionId) }
                                        .onSuccess { onMessage("Transaction deleted.") }
                                        .onFailure { onMessage(it.message ?: "This record couldn't be deleted.") }
                                }
                            }
                        )
                    }
                    MainTab.REPORTS -> ReportsScreen(summary = reportSummary, range = reportRange, onRangeChange = { reportRange = it })
                    MainTab.SETTINGS -> SettingsScreen(
                        preferences = preferences,
                        hasPin = !preferences.pinHash.isNullOrBlank() && !preferences.pinSalt.isNullOrBlank(),
                        onNotificationsChanged = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= 33) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    ReminderScheduler.ensureScheduled(context)
                                    scope.launch { container.preferences.setNotificationsEnabled(true) }
                                    onMessage("Payment reminders are on.")
                                }
                            } else {
                                ReminderScheduler.cancel(context)
                                scope.launch { container.preferences.setNotificationsEnabled(false) }
                                onMessage("Payment reminders are off.")
                            }
                        },
                        onAppLockChanged = { enabled ->
                            scope.launch {
                                if (enabled && (preferences.pinHash.isNullOrBlank() || preferences.pinSalt.isNullOrBlank())) {
                                    pendingPinSetup = true
                                } else {
                                    container.preferences.setAppLockEnabled(enabled)
                                    onMessage("Settings updated.")
                                }
                            }
                        },
                        onBiometricChanged = { enabled ->
                            val canUse = activity?.let { BiometricHelper(it).canAuthenticate() } == true
                            scope.launch {
                                if (enabled && !canUse) {
                                    onMessage("Biometric unlock isn't available on this device.")
                                } else {
                                    container.preferences.setBiometricEnabled(enabled)
                                    onMessage("Settings updated.")
                                }
                            }
                        },
                        onSetPin = { pendingPinSetup = true },
                        onClearPin = {
                            scope.launch {
                                container.preferences.clearPin()
                                onMessage("PIN removed.")
                            }
                        },
                        onCreateBackup = { backupLauncher.launch("KhataGo_Backup_${com.shohan.khatago.core.today()}.json") },
                        onRestoreBackup = { restoreLauncher.launch(arrayOf("application/json")) },
                        onExportCsv = { csvLauncher.launch("KhataGo_Transactions_${com.shohan.khatago.core.today()}.csv") },
                        onExportPdf = { pdfLauncher.launch("KhataGo_Report_${com.shohan.khatago.core.today()}.pdf") }
                    )
                }
            }
        }

        activeSheet?.let { sheet ->
            AddEntrySheet(
                type = sheet,
                categories = categories,
                paymentTarget = paymentTarget,
                onDismiss = { activeSheet = null },
                onSaveShopCredit = repository::addShopCredit,
                onSaveLoan = repository::addLoan,
                onSaveEmi = repository::addEmi,
                onSavePersonalDebt = repository::addPersonalDebt,
                onSaveIncome = repository::addIncome,
                onSaveExpense = repository::addExpense,
                onSavePayment = repository::addPayment,
                onMessage = onMessage
            )
        }

        if (pendingPinSetup) {
            PinSetupDialog(
                onDismiss = { pendingPinSetup = false },
                onSave = { pin ->
                    val secret = PinSecurity.create(pin)
                    scope.launch {
                        container.preferences.savePin(secret.hash, secret.salt)
                        pendingPinSetup = false
                        onMessage("PIN saved.")
                    }
                }
            )
        }

        if (pendingRestoreContent != null && pendingRestorePayload != null) {
            AlertDialog(
                onDismissRequest = {
                    pendingRestoreContent = null
                    pendingRestorePayload = null
                },
                title = { Text("Restore this backup?") },
                text = { Text("Your current data may be replaced by the backup.") },
                confirmButton = {
                    Button(onClick = {
                        val content = pendingRestoreContent.orEmpty()
                        scope.launch {
                            runCatching {
                                val restored = repository.restoreBackup(content)
                                container.preferences.applyBackup(restored.preferences)
                                if (restored.preferences.notificationsEnabled) ReminderScheduler.ensureScheduled(context) else ReminderScheduler.cancel(context)
                            }.onSuccess {
                                onMessage("Backup restored.")
                            }.onFailure {
                                onMessage(it.message ?: "Restore failed.")
                            }
                            pendingRestoreContent = null
                            pendingRestorePayload = null
                        }
                    }) { Text("Restore") }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        pendingRestoreContent = null
                        pendingRestorePayload = null
                    }) { Text("Cancel") }
                }
            )
        }
    }
}

private fun Context.writeTextToUri(uri: Uri, content: String) {
    contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
        writer.write(content)
    } ?: error("Output stream unavailable")
}

private fun Context.readTextFromUri(uri: Uri): String {
    return contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
        reader.readText()
    } ?: error("Input stream unavailable")
}
