package com.shohan.khatago.ui

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.shohan.khatago.AppContainer
import com.shohan.khatago.data.AccountsOverview
import com.shohan.khatago.data.preferences.AppPreferencesState
import com.shohan.khatago.domain.DashboardSnapshot
import com.shohan.khatago.domain.ReportRange
import com.shohan.khatago.domain.ReportSummary
import kotlinx.coroutines.launch

@Composable
fun KhataGoApp(container: AppContainer) {
    val navController = rememberNavController()
    val repository = container.repository
    val preferences by container.preferences.state.collectAsStateWithLifecycle(initialValue = AppPreferencesState())
    val profile by repository.userProfile.collectAsStateWithLifecycle(initialValue = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { _ ->
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
    val dashboard by repository.observeDashboard().collectAsStateWithLifecycle(initialValue = DashboardSnapshot())
    val accounts by repository.observeAccountsOverview().collectAsStateWithLifecycle(
        initialValue = AccountsOverview(0, emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    )
    val categories by repository.customCategories.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.DASHBOARD) }
    var activeSheet by remember { mutableStateOf<AddSheetType?>(null) }
    var paymentTarget by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var transactionQuery by rememberSaveable { mutableStateOf("") }
    var transactionMode by rememberSaveable { mutableStateOf("ALL") }
    var reportRange by rememberSaveable { mutableStateOf(ReportRange.THIS_MONTH) }
    val transactions by repository.observeTransactions(transactionQuery, transactionMode).collectAsStateWithLifecycle(initialValue = emptyList())
    val reportSummary by repository.observeReports(reportRange).collectAsStateWithLifecycle(
        initialValue = ReportSummary("This Month", 0, 0, 0, 0, emptyList(), emptyList(), emptyList())
    )
    val scope = rememberCoroutineScope()
    val currentDestination = selectedTab

    Scaffold(
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination == item,
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
                        "Add Personal Borrowed Money" to AddSheetType.PERSONAL_BORROWED,
                        "Add Personal Lent Money" to AddSheetType.PERSONAL_LENT,
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
            androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (selectedTab) {
                    MainTab.DASHBOARD -> DashboardScreen(
                        name = profileName,
                        snapshot = dashboard,
                        onSearch = onSearch,
                        onAction = { activeSheet = it },
                        onOpenDetail = { type, id -> onOpenDetail(type, id) },
                        onSeeAllTransactions = { selectedTab = MainTab.TRANSACTIONS }
                    )
                    MainTab.ACCOUNTS -> AccountsScreen(overview = accounts, onOpenDetail = onOpenDetail)
                    MainTab.TRANSACTIONS -> TransactionsScreen(
                        query = transactionQuery,
                        mode = transactionMode,
                        items = transactions,
                        onQueryChange = { transactionQuery = it },
                        onModeChange = { transactionMode = it }
                    )
                    MainTab.REPORTS -> ReportsScreen(summary = reportSummary, range = reportRange, onRangeChange = { reportRange = it })
                    MainTab.SETTINGS -> SettingsScreen(
                        preferences = preferences,
                        onNotificationsChanged = { value -> scope.launch { container.preferences.setNotificationsEnabled(value); onMessage("Settings updated.") } },
                        onAppLockChanged = { value -> scope.launch { container.preferences.setAppLockEnabled(value); onMessage("Settings updated.") } },
                        onBiometricChanged = { value -> scope.launch { container.preferences.setBiometricEnabled(value); onMessage("Settings updated.") } },
                        onCreateBackup = { onMessage("Backup export is not connected to file storage yet.") },
                        onRestoreBackup = { onMessage("Restore is not connected to file storage yet.") },
                        onExportCsv = { onMessage("CSV export is not connected yet.") },
                        onExportPdf = { onMessage("PDF export is not connected yet.") }
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
    }
}
