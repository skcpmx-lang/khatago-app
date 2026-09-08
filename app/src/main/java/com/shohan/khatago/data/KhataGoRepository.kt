package com.shohan.khatago.data

import androidx.room.withTransaction
import com.shohan.khatago.core.now
import com.shohan.khatago.core.today
import com.shohan.khatago.core.toLocalDate
import com.shohan.khatago.core.toLocalDateTime
import com.shohan.khatago.core.toStorage
import com.shohan.khatago.data.local.*
import com.shohan.khatago.data.preferences.AppPreferencesState
import com.shohan.khatago.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDate

class BusinessRuleException(message: String) : IllegalArgumentException(message)

@Serializable
data class BackupPayload(
    val backupVersion: Int = 1,
    val schemaVersion: Int = 1,
    val userProfile: UserProfileEntity? = null,
    val shops: List<ShopEntity> = emptyList(),
    val shopCredits: List<ShopCreditEntity> = emptyList(),
    val shopItems: List<ShopCreditItemEntity> = emptyList(),
    val shopPayments: List<ShopPaymentEntity> = emptyList(),
    val shopPaymentAllocations: List<ShopPaymentAllocationEntity> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val loanInstallments: List<LoanInstallmentEntity> = emptyList(),
    val loanPayments: List<LoanPaymentEntity> = emptyList(),
    val loanPaymentAllocations: List<LoanPaymentAllocationEntity> = emptyList(),
    val emiPurchases: List<EmiPurchaseEntity> = emptyList(),
    val emiInstallments: List<EmiInstallmentEntity> = emptyList(),
    val emiPayments: List<EmiPaymentEntity> = emptyList(),
    val emiPaymentAllocations: List<EmiPaymentAllocationEntity> = emptyList(),
    val people: List<PersonEntity> = emptyList(),
    val personalDebts: List<PersonalDebtEntity> = emptyList(),
    val personalSettlements: List<PersonalSettlementEntity> = emptyList(),
    val income: List<IncomeEntity> = emptyList(),
    val expenses: List<ExpenseEntity> = emptyList(),
    val customCategories: List<CustomCategoryEntity> = emptyList(),
    val transactions: List<FinancialTransactionEntity> = emptyList(),
    val preferences: AppPreferencesState = AppPreferencesState()
)

data class ShopCreditItemInput(
    val itemName: String,
    val quantityText: String,
    val unit: String,
    val unitPriceMinor: Long,
    val lineTotalMinor: Long
)

data class AddShopCreditInput(
    val shopName: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val notes: String,
    val purchaseDate: String,
    val dueDate: String?,
    val items: List<ShopCreditItemInput>,
    val purchaseNotes: String
)

data class AddLoanInput(
    val name: String,
    val institution: String,
    val loanAmountMinor: Long,
    val dateTaken: String,
    val interestRate: String,
    val processingFeeMinor: Long,
    val totalPayableMinor: Long,
    val installmentAmountMinor: Long,
    val installmentCount: Int,
    val frequency: Frequency,
    val firstDueDate: String,
    val notes: String
)

data class AddEmiInput(
    val productName: String,
    val provider: String,
    val purchaseDate: String,
    val totalPriceMinor: Long,
    val downPaymentMinor: Long,
    val financedAmountMinor: Long,
    val totalPayableMinor: Long,
    val installmentAmountMinor: Long,
    val installmentCount: Int,
    val frequency: Frequency,
    val firstDueDate: String,
    val notes: String
)

data class AddPersonalDebtInput(
    val personName: String,
    val relationship: String,
    val phone: String,
    val amountMinor: Long,
    val startedOn: String,
    val expectedDate: String?,
    val direction: PersonalDirection,
    val notes: String
)

data class AddIncomeInput(
    val occurredAt: String,
    val amountMinor: Long,
    val source: String,
    val category: String,
    val notes: String
)

data class AddExpenseInput(
    val occurredAt: String,
    val amountMinor: Long,
    val category: String,
    val place: String,
    val notes: String
)

data class AddPaymentInput(
    val targetType: String,
    val targetId: Long,
    val amountMinor: Long,
    val paidAt: String,
    val method: String,
    val notes: String
)

data class AccountsOverview(
    val totalOutstandingMinor: Long,
    val shopCredits: List<AccountListItem>,
    val loans: List<AccountListItem>,
    val emis: List<AccountListItem>,
    val personalBorrowed: List<AccountListItem>,
    val personalLent: List<AccountListItem>
)

data class ShopDetail(
    val shop: ShopEntity,
    val credits: List<ShopPurchaseDetail>,
    val payments: List<ShopPaymentEntity>,
    val totalMinor: Long,
    val paidMinor: Long,
    val remainingMinor: Long
)

data class ShopPurchaseDetail(
    val credit: ShopCreditEntity,
    val items: List<ShopCreditItemEntity>,
    val paidMinor: Long,
    val remainingMinor: Long,
    val status: PaymentStatus
)

data class LoanDetail(
    val loan: LoanEntity,
    val installments: List<DerivedInstallment>,
    val payments: List<LoanPaymentEntity>,
    val totalPaidMinor: Long,
    val remainingMinor: Long
)

data class EmiDetail(
    val emi: EmiPurchaseEntity,
    val installments: List<DerivedInstallment>,
    val payments: List<EmiPaymentEntity>,
    val totalPaidMinor: Long,
    val remainingMinor: Long
)

data class PersonalDebtDetail(
    val person: PersonEntity,
    val debt: PersonalDebtEntity,
    val settlements: List<PersonalSettlementEntity>,
    val settledMinor: Long,
    val remainingMinor: Long,
    val status: PaymentStatus
)

class KhataGoRepository(
    private val database: KhataGoDatabase,
    private val dao: KhataGoDao
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    val userProfile: Flow<UserProfileEntity?> = dao.observeUserProfile()
    val transactions: Flow<List<FinancialTransactionEntity>> = dao.observeTransactions()
    val customCategories: Flow<List<CustomCategoryEntity>> = dao.observeCustomCategories()

    suspend fun saveUserProfile(name: String) {
        dao.upsertUserProfile(UserProfileEntity(name = name.trim(), createdAt = now().toStorage()))
    }

    fun observeDashboard(): Flow<DashboardSnapshot> = combine(
        dao.observeShops(),
        dao.observeShopCredits(),
        dao.observeShopCreditItems(),
        dao.observeShopPayments(),
        dao.observeShopPaymentAllocations(),
        dao.observeLoans(),
        dao.observeLoanInstallments(),
        dao.observeLoanPayments(),
        dao.observeEmiPurchases(),
        dao.observeEmiInstallments(),
        dao.observeEmiPayments(),
        dao.observePeople(),
        dao.observePersonalDebts(),
        dao.observePersonalSettlements(),
        dao.observeIncome(),
        dao.observeExpenses(),
        dao.observeTransactions()
    ) { values ->
        val shops = values[0] as List<ShopEntity>
        val credits = values[1] as List<ShopCreditEntity>
        val items = values[2] as List<ShopCreditItemEntity>
        val shopPayments = values[3] as List<ShopPaymentEntity>
        val shopAllocations = values[4] as List<ShopPaymentAllocationEntity>
        val loans = values[5] as List<LoanEntity>
        val loanInstallments = values[6] as List<LoanInstallmentEntity>
        val loanPayments = values[7] as List<LoanPaymentEntity>
        val emis = values[8] as List<EmiPurchaseEntity>
        val emiInstallments = values[9] as List<EmiInstallmentEntity>
        val emiPayments = values[10] as List<EmiPaymentEntity>
        val people = values[11] as List<PersonEntity>
        val personalDebts = values[12] as List<PersonalDebtEntity>
        val personalSettlements = values[13] as List<PersonalSettlementEntity>
        val income = values[14] as List<IncomeEntity>
        val expenses = values[15] as List<ExpenseEntity>
        val transactions = values[16] as List<FinancialTransactionEntity>

        val shopAccounts = deriveShopAccounts(shops, credits, shopAllocations)
        val loanAccounts = deriveLoanAccounts(loans, loanInstallments)
        val emiAccounts = deriveEmiAccounts(emis, emiInstallments)
        val personalAccounts = derivePersonalAccounts(people, personalDebts, personalSettlements)
        val dueItems = buildDueItems(shops, credits, shopAllocations, loans, loanInstallments, emis, emiInstallments, people, personalDebts, personalSettlements)
        val today = today().toStorage()

        DashboardSnapshot(
            totalOutstandingMinor = shopAccounts.sumOf { it.remainingAmountMinor } +
                loanAccounts.sumOf { it.remainingAmountMinor } +
                emiAccounts.sumOf { it.remainingAmountMinor } +
                personalAccounts.filter { it.type == "PERSONAL_BORROWED" }.sumOf { it.remainingAmountMinor },
            shopOutstandingMinor = shopAccounts.sumOf { it.remainingAmountMinor },
            loanOutstandingMinor = loanAccounts.sumOf { it.remainingAmountMinor },
            emiOutstandingMinor = emiAccounts.sumOf { it.remainingAmountMinor },
            personalOutstandingMinor = personalAccounts.filter { it.type == "PERSONAL_BORROWED" }.sumOf { it.remainingAmountMinor },
            overdueCount = dueItems.count { it.status == PaymentStatus.OVERDUE },
            overdueMinor = dueItems.filter { it.status == PaymentStatus.OVERDUE }.sumOf { it.amountMinor },
            dueTodayItems = dueItems.filter { it.status == PaymentStatus.DUE_TODAY },
            upcomingItems = dueItems.filter { it.status == PaymentStatus.UPCOMING }.sortedBy { it.dueDate }.take(6),
            todayIncomeMinor = income.filter { it.occurredAt.startsWith(today) }.sumOf { it.amountMinor },
            todayExpenseMinor = expenses.filter { it.occurredAt.startsWith(today) }.sumOf { it.amountMinor },
            todayPaymentsMinor = transactions.filter {
                it.occurredAt.startsWith(today) && it.type in paymentTypes
            }.sumOf { it.amountMinor },
            recentTransactions = transactions.take(8).map { it.toListItem() },
            moneyOverview = buildMoneyOverview(transactions),
        )
    }

    fun observeAccountsOverview(): Flow<AccountsOverview> = combine(
        dao.observeShops(),
        dao.observeShopCredits(),
        dao.observeShopPaymentAllocations(),
        dao.observeLoans(),
        dao.observeLoanInstallments(),
        dao.observeEmiPurchases(),
        dao.observeEmiInstallments(),
        dao.observePeople(),
        dao.observePersonalDebts(),
        dao.observePersonalSettlements()
    ) { values ->
        val shops = values[0] as List<ShopEntity>
        val credits = values[1] as List<ShopCreditEntity>
        val shopAllocations = values[2] as List<ShopPaymentAllocationEntity>
        val loans = values[3] as List<LoanEntity>
        val loanInstallments = values[4] as List<LoanInstallmentEntity>
        val emis = values[5] as List<EmiPurchaseEntity>
        val emiInstallments = values[6] as List<EmiInstallmentEntity>
        val people = values[7] as List<PersonEntity>
        val personalDebts = values[8] as List<PersonalDebtEntity>
        val personalSettlements = values[9] as List<PersonalSettlementEntity>

        val shopAccounts = deriveShopAccounts(shops, credits, shopAllocations)
        val loanAccounts = deriveLoanAccounts(loans, loanInstallments)
        val emiAccounts = deriveEmiAccounts(emis, emiInstallments)
        val personalAccounts = derivePersonalAccounts(people, personalDebts, personalSettlements)
        AccountsOverview(
            totalOutstandingMinor = shopAccounts.sumOf { it.remainingAmountMinor } +
                loanAccounts.sumOf { it.remainingAmountMinor } +
                emiAccounts.sumOf { it.remainingAmountMinor } +
                personalAccounts.filter { it.type == "PERSONAL_BORROWED" }.sumOf { it.remainingAmountMinor },
            shopCredits = shopAccounts,
            loans = loanAccounts,
            emis = emiAccounts,
            personalBorrowed = personalAccounts.filter { it.type == "PERSONAL_BORROWED" },
            personalLent = personalAccounts.filter { it.type == "PERSONAL_LENT" }
        )
    }

    fun observeTransactions(query: String = "", mode: String = "ALL"): Flow<List<TransactionListItem>> =
        dao.observeTransactions().combine(dao.observePeople()) { transactions, _ ->
            transactions
                .filter { txn ->
                    query.isBlank() || listOf(txn.title, txn.category, txn.relatedName, txn.notes)
                        .joinToString(" ")
                        .contains(query.trim(), ignoreCase = true)
                }
                .filter { txn ->
                    when (mode) {
                        "INCOME" -> txn.type == TransactionType.INCOME
                        "EXPENSE" -> txn.type == TransactionType.EXPENSE
                        "PAYMENTS" -> txn.type in paymentTypes
                        else -> true
                    }
                }
                .map { it.toListItem() }
        }

    fun observeReports(range: ReportRange): Flow<ReportSummary> = combine(
        dao.observeTransactions(),
        dao.observeShops(),
        dao.observeShopCredits(),
        dao.observeShopPaymentAllocations(),
        dao.observeLoans(),
        dao.observeLoanInstallments(),
        dao.observeEmiPurchases(),
        dao.observeEmiInstallments(),
        dao.observePeople(),
        dao.observePersonalDebts(),
        dao.observePersonalSettlements()
    ) { values ->
        val transactions = values[0] as List<FinancialTransactionEntity>
        val shops = values[1] as List<ShopEntity>
        val credits = values[2] as List<ShopCreditEntity>
        val shopAllocations = values[3] as List<ShopPaymentAllocationEntity>
        val loans = values[4] as List<LoanEntity>
        val loanInstallments = values[5] as List<LoanInstallmentEntity>
        val emis = values[6] as List<EmiPurchaseEntity>
        val emiInstallments = values[7] as List<EmiInstallmentEntity>
        val people = values[8] as List<PersonEntity>
        val personalDebts = values[9] as List<PersonalDebtEntity>
        val personalSettlements = values[10] as List<PersonalSettlementEntity>

        val dateRange = range.resolveDateRange()
        val filtered = transactions.filter { txn ->
            val date = txn.occurredAt.substring(0, 10).toLocalDate()
            !date.isBefore(dateRange.first) && !date.isAfter(dateRange.second)
        }
        val shopAccounts = deriveShopAccounts(shops, credits, shopAllocations)
        val loanAccounts = deriveLoanAccounts(loans, loanInstallments)
        val emiAccounts = deriveEmiAccounts(emis, emiInstallments)
        val personalAccounts = derivePersonalAccounts(people, personalDebts, personalSettlements)
        val distribution = listOf(
            DistributionItem("Shop Credit", shopAccounts.sumOf { it.remainingAmountMinor }),
            DistributionItem("Loans", loanAccounts.sumOf { it.remainingAmountMinor }),
            DistributionItem("EMI", emiAccounts.sumOf { it.remainingAmountMinor }),
            DistributionItem("Personal Debt", personalAccounts.filter { it.type == "PERSONAL_BORROWED" }.sumOf { it.remainingAmountMinor })
        )
        val incomeMinor = filtered.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor }
        val expenseMinor = filtered.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor }
        val paymentMinor = filtered.filter { it.type in paymentTypes }.sumOf { it.amountMinor }
        ReportSummary(
            label = range.label,
            incomeMinor = incomeMinor,
            expenseMinor = expenseMinor,
            paymentMinor = paymentMinor,
            outstandingMinor = distribution.sumOf { it.amountMinor },
            debtDistribution = distribution,
            monthlyOverview = buildMoneyOverview(filtered),
            insights = InsightEngine.build(
                incomeMinor = incomeMinor,
                expenseMinor = expenseMinor,
                overdueMinor = buildDueItems(shops, credits, shopAllocations, loans, loanInstallments, emis, emiInstallments, people, personalDebts, personalSettlements)
                    .filter { it.status == PaymentStatus.OVERDUE }
                    .sumOf { it.amountMinor },
                upcomingCount = buildDueItems(shops, credits, shopAllocations, loans, loanInstallments, emis, emiInstallments, people, personalDebts, personalSettlements)
                    .count { it.status == PaymentStatus.UPCOMING },
                largestBalance = distribution.maxByOrNull { it.amountMinor }
            )
        )
    }

    fun observeSearch(query: String): Flow<SearchResults> = combine(
        observeTransactions(query, "ALL"),
        observeAccountsOverview()
    ) { transactions, accounts ->
        val accountItems = (accounts.shopCredits + accounts.loans + accounts.emis + accounts.personalBorrowed + accounts.personalLent)
            .filter {
                query.isBlank() || listOf(it.title, it.subtitle).joinToString(" ").contains(query, ignoreCase = true)
            }
        SearchResults(transactions = transactions.take(10), accounts = accountItems.take(10))
    }

    fun observeShopDetail(id: Long): Flow<ShopDetail?> = combine(
        dao.observeShops(),
        dao.observeShopCredits(),
        dao.observeShopCreditItems(),
        dao.observeShopPayments(),
        dao.observeShopPaymentAllocations()
    ) { shops, credits, items, payments, allocations ->
        val shop = shops.firstOrNull { it.id == id } ?: return@combine null
        val shopCredits = credits.filter { it.shopId == id }.sortedByDescending { it.purchaseDate }
        val itemMap = items.groupBy { it.creditId }
        val allocationMap = allocations.groupBy { it.creditId }
        val total = shopCredits.sumOf { it.totalAmountMinor }
        val paid = shopCredits.sumOf { credit -> allocationMap[credit.id].orEmpty().sumOf { it.amountMinor } }
        ShopDetail(
            shop = shop,
            credits = shopCredits.map { credit ->
                val creditPaid = allocationMap[credit.id].orEmpty().sumOf { it.amountMinor }
                val remaining = maxOf(0L, credit.totalAmountMinor - creditPaid)
                ShopPurchaseDetail(
                    credit = credit,
                    items = itemMap[credit.id].orEmpty(),
                    paidMinor = creditPaid,
                    remainingMinor = remaining,
                    status = deriveCreditStatus(credit.dueDate, credit.totalAmountMinor, creditPaid)
                )
            },
            payments = payments.filter { it.shopId == id },
            totalMinor = total,
            paidMinor = paid,
            remainingMinor = maxOf(0L, total - paid)
        )
    }

    fun observeLoanDetail(id: Long): Flow<LoanDetail?> = combine(
        dao.observeLoans(),
        dao.observeLoanInstallments(),
        dao.observeLoanPayments()
    ) { loans, installments, payments ->
        val loan = loans.firstOrNull { it.id == id } ?: return@combine null
        val loanInstallments = installments.filter { it.loanId == id }.map { it.toDerived() }
        val totalPaid = loanInstallments.sumOf { it.paidAmountMinor }
        LoanDetail(
            loan = loan,
            installments = loanInstallments,
            payments = payments.filter { it.loanId == id },
            totalPaidMinor = totalPaid,
            remainingMinor = maxOf(0L, loan.totalPayableMinor - totalPaid)
        )
    }

    fun observeEmiDetail(id: Long): Flow<EmiDetail?> = combine(
        dao.observeEmiPurchases(),
        dao.observeEmiInstallments(),
        dao.observeEmiPayments()
    ) { emis, installments, payments ->
        val emi = emis.firstOrNull { it.id == id } ?: return@combine null
        val emiInstallments = installments.filter { it.emiId == id }.map { it.toDerived() }
        val totalPaid = emiInstallments.sumOf { it.paidAmountMinor }
        EmiDetail(
            emi = emi,
            installments = emiInstallments,
            payments = payments.filter { it.emiId == id },
            totalPaidMinor = totalPaid,
            remainingMinor = maxOf(0L, emi.totalPayableMinor - totalPaid)
        )
    }

    fun observePersonalDebtDetail(id: Long): Flow<PersonalDebtDetail?> = combine(
        dao.observePeople(),
        dao.observePersonalDebts(),
        dao.observePersonalSettlements()
    ) { people, debts, settlements ->
        val debt = debts.firstOrNull { it.id == id } ?: return@combine null
        val person = people.firstOrNull { it.id == debt.personId } ?: return@combine null
        val debtSettlements = settlements.filter { it.debtId == id }
        val settled = debtSettlements.sumOf { it.amountMinor }
        val remaining = maxOf(0L, debt.amountMinor - settled)
        PersonalDebtDetail(
            person = person,
            debt = debt,
            settlements = debtSettlements,
            settledMinor = settled,
            remainingMinor = remaining,
            status = when {
                remaining == 0L -> PaymentStatus.PAID
                settled > 0L -> deriveCreditStatus(debt.expectedDate, debt.amountMinor, settled)
                else -> deriveCreditStatus(debt.expectedDate, debt.amountMinor, 0)
            }
        )
    }

    suspend fun addShopCredit(input: AddShopCreditInput) {
        require(input.shopName.isNotBlank()) { "Please complete the required fields." }
        require(input.items.isNotEmpty()) { "Add at least one item." }
        val total = input.items.sumOf { it.lineTotalMinor }
        database.withTransaction {
            val shopId = findOrCreateShop(input.shopName, input.ownerName, input.phone, input.address, input.notes)
            val creditId = dao.insertShopCredit(
                ShopCreditEntity(
                    shopId = shopId,
                    purchaseDate = input.purchaseDate,
                    dueDate = input.dueDate,
                    totalAmountMinor = total,
                    notes = input.purchaseNotes,
                    createdAt = now().toStorage()
                )
            )
            dao.insertShopCreditItems(input.items.map {
                ShopCreditItemEntity(
                    creditId = creditId,
                    itemName = it.itemName,
                    quantityText = it.quantityText,
                    unit = it.unit,
                    unitPriceMinor = it.unitPriceMinor,
                    lineTotalMinor = it.lineTotalMinor
                )
            })
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = now().toStorage(),
                    title = input.shopName,
                    category = "Shop Credit",
                    amountMinor = total,
                    type = TransactionType.SHOP_CREDIT,
                    relatedName = input.shopName,
                    relatedEntityType = "SHOP",
                    relatedEntityId = shopId,
                    cashEffect = TransactionCashEffect.NEUTRAL,
                    statusLabel = input.dueDate?.let { deriveCreditStatus(it, total, 0).label } ?: "Open",
                    notes = input.purchaseNotes
                )
            )
        }
    }

    suspend fun addLoan(input: AddLoanInput) {
        require(input.name.isNotBlank()) { "Please complete the required fields." }
        val firstDueDate = input.firstDueDate.toLocalDate()
        val schedule = InstallmentScheduleGenerator.generate(
            totalPayableMinor = input.totalPayableMinor,
            installmentCount = input.installmentCount,
            firstDueDate = firstDueDate,
            frequency = input.frequency,
            preferredInstallmentMinor = input.installmentAmountMinor
        )
        database.withTransaction {
            val loanId = dao.insertLoan(
                LoanEntity(
                    name = input.name,
                    institution = input.institution,
                    loanAmountMinor = input.loanAmountMinor,
                    dateTaken = input.dateTaken,
                    interestRate = input.interestRate,
                    processingFeeMinor = input.processingFeeMinor,
                    totalPayableMinor = input.totalPayableMinor,
                    installmentAmountMinor = input.installmentAmountMinor,
                    frequency = input.frequency,
                    installmentCount = input.installmentCount,
                    firstDueDate = input.firstDueDate,
                    maturityDate = schedule.last().dueDate,
                    notes = input.notes,
                    createdAt = now().toStorage()
                )
            )
            dao.insertLoanInstallments(schedule.map {
                LoanInstallmentEntity(
                    loanId = loanId,
                    installmentNumber = it.number,
                    dueDate = it.dueDate,
                    scheduledAmountMinor = it.amountMinor
                )
            })
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = now().toStorage(),
                    title = input.name,
                    category = "Loan",
                    amountMinor = input.totalPayableMinor,
                    type = TransactionType.LOAN,
                    relatedName = input.institution,
                    relatedEntityType = "LOAN",
                    relatedEntityId = loanId,
                    cashEffect = TransactionCashEffect.NEUTRAL,
                    statusLabel = "Created",
                    notes = input.notes
                )
            )
        }
    }

    suspend fun addEmi(input: AddEmiInput) {
        require(input.productName.isNotBlank()) { "Please complete the required fields." }
        val schedule = InstallmentScheduleGenerator.generate(
            totalPayableMinor = input.totalPayableMinor,
            installmentCount = input.installmentCount,
            firstDueDate = input.firstDueDate.toLocalDate(),
            frequency = input.frequency,
            preferredInstallmentMinor = input.installmentAmountMinor
        )
        database.withTransaction {
            val emiId = dao.insertEmiPurchase(
                EmiPurchaseEntity(
                    productName = input.productName,
                    provider = input.provider,
                    purchaseDate = input.purchaseDate,
                    totalPriceMinor = input.totalPriceMinor,
                    downPaymentMinor = input.downPaymentMinor,
                    financedAmountMinor = input.financedAmountMinor,
                    totalPayableMinor = input.totalPayableMinor,
                    installmentAmountMinor = input.installmentAmountMinor,
                    installmentCount = input.installmentCount,
                    frequency = input.frequency,
                    firstDueDate = input.firstDueDate,
                    notes = input.notes,
                    createdAt = now().toStorage()
                )
            )
            dao.insertEmiInstallments(schedule.map {
                EmiInstallmentEntity(
                    emiId = emiId,
                    installmentNumber = it.number,
                    dueDate = it.dueDate,
                    scheduledAmountMinor = it.amountMinor
                )
            })
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = now().toStorage(),
                    title = input.productName,
                    category = "EMI",
                    amountMinor = input.totalPayableMinor,
                    type = TransactionType.EMI_PURCHASE,
                    relatedName = input.provider,
                    relatedEntityType = "EMI",
                    relatedEntityId = emiId,
                    cashEffect = TransactionCashEffect.NEUTRAL,
                    statusLabel = "Created",
                    notes = input.notes
                )
            )
        }
    }

    suspend fun addPersonalDebt(input: AddPersonalDebtInput) {
        require(input.personName.isNotBlank()) { "Please complete the required fields." }
        database.withTransaction {
            val personId = findOrCreatePerson(input.personName, input.relationship, input.phone, input.notes)
            val debtId = dao.insertPersonalDebt(
                PersonalDebtEntity(
                    personId = personId,
                    direction = input.direction,
                    amountMinor = input.amountMinor,
                    startedOn = input.startedOn,
                    expectedDate = input.expectedDate,
                    notes = input.notes,
                    createdAt = now().toStorage()
                )
            )
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = now().toStorage(),
                    title = input.personName,
                    category = if (input.direction == PersonalDirection.BORROWED) "Personal Borrowed" else "Personal Lent",
                    amountMinor = input.amountMinor,
                    type = if (input.direction == PersonalDirection.BORROWED) TransactionType.PERSONAL_BORROWING else TransactionType.PERSONAL_LENDING,
                    relatedName = input.relationship,
                    relatedEntityType = "PERSONAL_DEBT",
                    relatedEntityId = debtId,
                    cashEffect = TransactionCashEffect.NEUTRAL,
                    statusLabel = "Open",
                    notes = input.notes
                )
            )
        }
    }

    suspend fun addIncome(input: AddIncomeInput) {
        val incomeId = dao.insertIncome(
            IncomeEntity(
                occurredAt = input.occurredAt,
                amountMinor = input.amountMinor,
                source = input.source,
                category = input.category,
                notes = input.notes
            )
        )
        dao.insertTransaction(
            FinancialTransactionEntity(
                occurredAt = input.occurredAt,
                title = input.source,
                category = input.category,
                amountMinor = input.amountMinor,
                type = TransactionType.INCOME,
                relatedName = input.source,
                relatedEntityType = "INCOME",
                relatedEntityId = incomeId,
                cashEffect = TransactionCashEffect.IN,
                statusLabel = "Recorded",
                notes = input.notes
            )
        )
        dao.insertCustomCategory(CustomCategoryEntity(kind = "INCOME", name = input.category))
    }

    suspend fun addExpense(input: AddExpenseInput) {
        val expenseId = dao.insertExpense(
            ExpenseEntity(
                occurredAt = input.occurredAt,
                amountMinor = input.amountMinor,
                category = input.category,
                place = input.place,
                notes = input.notes
            )
        )
        dao.insertTransaction(
            FinancialTransactionEntity(
                occurredAt = input.occurredAt,
                title = if (input.place.isBlank()) input.category else input.place,
                category = input.category,
                amountMinor = input.amountMinor,
                type = TransactionType.EXPENSE,
                relatedName = input.place,
                relatedEntityType = "EXPENSE",
                relatedEntityId = expenseId,
                cashEffect = TransactionCashEffect.OUT,
                statusLabel = "Recorded",
                notes = input.notes
            )
        )
        dao.insertCustomCategory(CustomCategoryEntity(kind = "EXPENSE", name = input.category))
    }

    suspend fun addPayment(input: AddPaymentInput) {
        when (input.targetType) {
            "SHOP" -> addShopPayment(input.targetId, input.amountMinor, input.paidAt, input.method, input.notes)
            "LOAN" -> addLoanPayment(input.targetId, input.amountMinor, input.paidAt, input.method, input.notes)
            "EMI" -> addEmiPayment(input.targetId, input.amountMinor, input.paidAt, input.method, input.notes)
            "PERSONAL" -> addPersonalSettlement(input.targetId, input.amountMinor, input.paidAt, input.notes)
        }
    }

    suspend fun archiveAccount(type: String, id: Long, archived: Boolean = true) {
        when (type) {
            "SHOP" -> dao.setShopArchived(id, archived)
            "LOAN" -> dao.setLoanArchived(id, archived)
            "EMI" -> dao.setEmiArchived(id, archived)
            "PERSONAL" -> dao.setPersonalDebtArchived(id, archived)
        }
    }

    suspend fun deleteAccount(type: String, id: Long) {
        database.withTransaction {
            when (type) {
                "SHOP" -> {
                    dao.deleteTransactionsByRelation("SHOP", id)
                    dao.deleteShop(id)
                }
                "LOAN" -> {
                    dao.deleteTransactionsByRelation("LOAN", id)
                    dao.deleteLoan(id)
                }
                "EMI" -> {
                    dao.deleteTransactionsByRelation("EMI", id)
                    dao.deleteEmi(id)
                }
                "PERSONAL" -> {
                    dao.deleteTransactionsByRelation("PERSONAL_DEBT", id)
                    dao.deletePersonalDebt(id)
                }
            }
        }
    }

    suspend fun deleteCashTransaction(transactionId: Long) {
        database.withTransaction {
            val transaction = dao.getTransactionById(transactionId) ?: throw BusinessRuleException("Transaction not found.")
            when (transaction.type) {
                TransactionType.INCOME -> if (transaction.relatedEntityId > 0L) dao.deleteIncome(transaction.relatedEntityId)
                TransactionType.EXPENSE -> if (transaction.relatedEntityId > 0L) dao.deleteExpense(transaction.relatedEntityId)
                else -> throw BusinessRuleException("Delete this record from its account screen.")
            }
            dao.deleteTransaction(transactionId)
        }
    }

    suspend fun addShopPayment(shopId: Long, amountMinor: Long, paidAt: String, method: String, notes: String) {
        database.withTransaction {
            val shops = dao.observeShops().firstValue()
            val credits = dao.observeShopCredits().firstValue().filter { it.shopId == shopId }
            val allocations = dao.observeShopPaymentAllocations().firstValue()
            val shop = shops.firstOrNull { it.id == shopId } ?: throw BusinessRuleException("Shop not found.")
            val outstanding = credits.sumOf { credit ->
                val paid = allocations.filter { it.creditId == credit.id }.sumOf { it.amountMinor }
                maxOf(0L, credit.totalAmountMinor - paid)
            }
            if (amountMinor > outstanding) throw BusinessRuleException("This payment is higher than the amount due.")
            val paymentId = dao.insertShopPayment(ShopPaymentEntity(shopId = shopId, paidAt = paidAt, amountMinor = amountMinor, method = method, notes = notes))
            val newAllocations = mutableListOf<ShopPaymentAllocationEntity>()
            var remaining = amountMinor
            credits.sortedBy { it.purchaseDate }.forEach { credit ->
                if (remaining == 0L) return@forEach
                val paid = allocations.filter { it.creditId == credit.id }.sumOf { it.amountMinor }
                val due = maxOf(0L, credit.totalAmountMinor - paid)
                if (due > 0L) {
                    val applied = minOf(due, remaining)
                    newAllocations += ShopPaymentAllocationEntity(paymentId = paymentId, creditId = credit.id, amountMinor = applied)
                    remaining -= applied
                }
            }
            dao.insertShopPaymentAllocations(newAllocations)
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = paidAt,
                    title = "${shop.name} payment",
                    category = "Payment",
                    amountMinor = amountMinor,
                    type = TransactionType.SHOP_PAYMENT,
                    relatedName = shop.name,
                    relatedEntityType = "SHOP",
                    relatedEntityId = shopId,
                    cashEffect = TransactionCashEffect.OUT,
                    statusLabel = "Paid",
                    notes = notes
                )
            )
        }
    }

    suspend fun addLoanPayment(loanId: Long, amountMinor: Long, paidAt: String, method: String, notes: String) {
        database.withTransaction {
            val loans = dao.observeLoans().firstValue()
            val installments = dao.observeLoanInstallments().firstValue().filter { it.loanId == loanId }.sortedBy { it.installmentNumber }
            val loan = loans.firstOrNull { it.id == loanId } ?: throw BusinessRuleException("Loan not found.")
            val remainingTotal = installments.sumOf { maxOf(0L, it.scheduledAmountMinor - it.paidAmountMinor) }
            if (amountMinor > remainingTotal) throw BusinessRuleException("This payment is higher than the amount due.")
            val paymentId = dao.insertLoanPayment(LoanPaymentEntity(loanId = loanId, paidAt = paidAt, amountMinor = amountMinor, method = method, notes = notes))
            var remaining = amountMinor
            val updatedInstallments = mutableListOf<LoanInstallmentEntity>()
            val allocations = mutableListOf<LoanPaymentAllocationEntity>()
            installments.forEach { installment ->
                if (remaining == 0L) return@forEach
                val due = maxOf(0L, installment.scheduledAmountMinor - installment.paidAmountMinor)
                if (due > 0L) {
                    val applied = minOf(remaining, due)
                    updatedInstallments += installment.copy(paidAmountMinor = installment.paidAmountMinor + applied)
                    allocations += LoanPaymentAllocationEntity(paymentId = paymentId, installmentId = installment.id, amountMinor = applied)
                    remaining -= applied
                }
            }
            dao.updateLoanInstallments(updatedInstallments)
            dao.insertLoanPaymentAllocations(allocations)
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = paidAt,
                    title = "${loan.name} payment",
                    category = "Payment",
                    amountMinor = amountMinor,
                    type = TransactionType.LOAN_PAYMENT,
                    relatedName = loan.institution,
                    relatedEntityType = "LOAN",
                    relatedEntityId = loanId,
                    cashEffect = TransactionCashEffect.OUT,
                    statusLabel = "Paid",
                    notes = notes
                )
            )
        }
    }

    suspend fun addEmiPayment(emiId: Long, amountMinor: Long, paidAt: String, method: String, notes: String) {
        database.withTransaction {
            val emis = dao.observeEmiPurchases().firstValue()
            val installments = dao.observeEmiInstallments().firstValue().filter { it.emiId == emiId }.sortedBy { it.installmentNumber }
            val emi = emis.firstOrNull { it.id == emiId } ?: throw BusinessRuleException("EMI not found.")
            val remainingTotal = installments.sumOf { maxOf(0L, it.scheduledAmountMinor - it.paidAmountMinor) }
            if (amountMinor > remainingTotal) throw BusinessRuleException("This payment is higher than the amount due.")
            val paymentId = dao.insertEmiPayment(EmiPaymentEntity(emiId = emiId, paidAt = paidAt, amountMinor = amountMinor, method = method, notes = notes))
            var remaining = amountMinor
            val updatedInstallments = mutableListOf<EmiInstallmentEntity>()
            val allocations = mutableListOf<EmiPaymentAllocationEntity>()
            installments.forEach { installment ->
                if (remaining == 0L) return@forEach
                val due = maxOf(0L, installment.scheduledAmountMinor - installment.paidAmountMinor)
                if (due > 0L) {
                    val applied = minOf(remaining, due)
                    updatedInstallments += installment.copy(paidAmountMinor = installment.paidAmountMinor + applied)
                    allocations += EmiPaymentAllocationEntity(paymentId = paymentId, installmentId = installment.id, amountMinor = applied)
                    remaining -= applied
                }
            }
            dao.updateEmiInstallments(updatedInstallments)
            dao.insertEmiPaymentAllocations(allocations)
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = paidAt,
                    title = "${emi.productName} payment",
                    category = "Payment",
                    amountMinor = amountMinor,
                    type = TransactionType.EMI_PAYMENT,
                    relatedName = emi.provider,
                    relatedEntityType = "EMI",
                    relatedEntityId = emiId,
                    cashEffect = TransactionCashEffect.OUT,
                    statusLabel = "Paid",
                    notes = notes
                )
            )
        }
    }

    suspend fun addPersonalSettlement(debtId: Long, amountMinor: Long, settledAt: String, notes: String) {
        database.withTransaction {
            val debts = dao.observePersonalDebts().firstValue()
            val people = dao.observePeople().firstValue()
            val settlements = dao.observePersonalSettlements().firstValue().filter { it.debtId == debtId }
            val debt = debts.firstOrNull { it.id == debtId } ?: throw BusinessRuleException("Record not found.")
            val person = people.firstOrNull { it.id == debt.personId } ?: throw BusinessRuleException("Person not found.")
            val settled = settlements.sumOf { it.amountMinor }
            val remaining = maxOf(0L, debt.amountMinor - settled)
            if (amountMinor > remaining) throw BusinessRuleException("This payment is higher than the amount due.")
            dao.insertPersonalSettlement(PersonalSettlementEntity(debtId = debtId, settledAt = settledAt, amountMinor = amountMinor, notes = notes))
            dao.insertTransaction(
                FinancialTransactionEntity(
                    occurredAt = settledAt,
                    title = if (debt.direction == PersonalDirection.BORROWED) "Repayment to ${person.name}" else "Return from ${person.name}",
                    category = "Payment",
                    amountMinor = amountMinor,
                    type = if (debt.direction == PersonalDirection.BORROWED) TransactionType.PERSONAL_REPAYMENT else TransactionType.PERSONAL_RETURN,
                    relatedName = person.name,
                    relatedEntityType = "PERSONAL_DEBT",
                    relatedEntityId = debtId,
                    cashEffect = if (debt.direction == PersonalDirection.BORROWED) TransactionCashEffect.OUT else TransactionCashEffect.IN,
                    statusLabel = "Paid",
                    notes = notes
                )
            )
        }
    }

    suspend fun exportBackup(preferences: AppPreferencesState): String = json.encodeToString(
        BackupPayload(
            userProfile = dao.observeUserProfile().firstValue(),
            shops = dao.observeShops().firstValue(),
            shopCredits = dao.observeShopCredits().firstValue(),
            shopItems = dao.observeShopCreditItems().firstValue(),
            shopPayments = dao.observeShopPayments().firstValue(),
            shopPaymentAllocations = dao.observeShopPaymentAllocations().firstValue(),
            loans = dao.observeLoans().firstValue(),
            loanInstallments = dao.observeLoanInstallments().firstValue(),
            loanPayments = dao.observeLoanPayments().firstValue(),
            loanPaymentAllocations = dao.observeLoanPaymentAllocations().firstValue(),
            emiPurchases = dao.observeEmiPurchases().firstValue(),
            emiInstallments = dao.observeEmiInstallments().firstValue(),
            emiPayments = dao.observeEmiPayments().firstValue(),
            emiPaymentAllocations = dao.observeEmiPaymentAllocations().firstValue(),
            people = dao.observePeople().firstValue(),
            personalDebts = dao.observePersonalDebts().firstValue(),
            personalSettlements = dao.observePersonalSettlements().firstValue(),
            income = dao.observeIncome().firstValue(),
            expenses = dao.observeExpenses().firstValue(),
            customCategories = dao.observeCustomCategories().firstValue(),
            transactions = dao.observeTransactions().firstValue(),
            preferences = preferences
        )
    )

    fun parseBackup(content: String): BackupPayload = try {
        json.decodeFromString<BackupPayload>(content)
    } catch (_: Exception) {
        throw BusinessRuleException("This backup file can't be read.")
    }

    suspend fun restoreBackup(content: String): BackupPayload {
        val payload = parseBackup(content)
        validateBackup(payload)
        database.withTransaction {
            dao.clearTransactions()
            dao.clearShopPaymentAllocations()
            dao.clearShopPayments()
            dao.clearShopCreditItems()
            dao.clearShopCredits()
            dao.clearShops()
            dao.clearLoanPaymentAllocations()
            dao.clearLoanPayments()
            dao.clearLoanInstallments()
            dao.clearLoans()
            dao.clearEmiPaymentAllocations()
            dao.clearEmiPayments()
            dao.clearEmiInstallments()
            dao.clearEmiPurchases()
            dao.clearPersonalSettlements()
            dao.clearPersonalDebts()
            dao.clearPeople()
            dao.clearIncome()
            dao.clearExpenses()
            dao.clearCustomCategories()
            dao.clearUserProfile()

            payload.userProfile?.let { dao.upsertUserProfile(it) }
            payload.shops.forEach { dao.insertShop(it) }
            payload.shopCredits.forEach { dao.insertShopCredit(it) }
            payload.shopItems.chunked(100).forEach { dao.insertShopCreditItems(it) }
            payload.shopPayments.forEach { dao.insertShopPayment(it) }
            payload.shopPaymentAllocations.chunked(100).forEach { dao.insertShopPaymentAllocations(it) }
            payload.loans.forEach { dao.insertLoan(it) }
            payload.loanInstallments.chunked(100).forEach { dao.insertLoanInstallments(it) }
            payload.loanPayments.forEach { dao.insertLoanPayment(it) }
            payload.loanPaymentAllocations.chunked(100).forEach { dao.insertLoanPaymentAllocations(it) }
            payload.emiPurchases.forEach { dao.insertEmiPurchase(it) }
            payload.emiInstallments.chunked(100).forEach { dao.insertEmiInstallments(it) }
            payload.emiPayments.forEach { dao.insertEmiPayment(it) }
            payload.emiPaymentAllocations.chunked(100).forEach { dao.insertEmiPaymentAllocations(it) }
            payload.people.forEach { dao.insertPerson(it) }
            payload.personalDebts.forEach { dao.insertPersonalDebt(it) }
            payload.personalSettlements.forEach { dao.insertPersonalSettlement(it) }
            payload.income.forEach { dao.insertIncome(it) }
            payload.expenses.forEach { dao.insertExpense(it) }
            payload.customCategories.forEach { dao.insertCustomCategory(it) }
            payload.transactions.chunked(100).forEach { dao.insertTransactions(it) }
        }
        return payload
    }

    suspend fun exportTransactionsCsv(): String = com.shohan.khatago.export.CsvExporter.export(
        dao.observeTransactions().firstValue()
    )

    fun validateBackup(payload: BackupPayload) {
        BackupValidator.validate(payload)
    }

    companion object {
        val defaultIncomeCategories = listOf("Salary", "Business", "Freelance", "Bonus", "Gift", "Refund", "Other")
        val defaultExpenseCategories = listOf("Food", "Groceries", "Transport", "Home", "Medical", "Education", "Bills", "Shopping", "Entertainment", "Other")
        val paymentTypes = setOf(
            TransactionType.SHOP_PAYMENT,
            TransactionType.LOAN_PAYMENT,
            TransactionType.EMI_PAYMENT,
            TransactionType.PERSONAL_REPAYMENT,
            TransactionType.PERSONAL_RETURN
        )
    }

    private suspend fun findOrCreateShop(name: String, ownerName: String, phone: String, address: String, notes: String): Long {
        val existing = dao.observeShops().firstValue().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
        return existing?.id ?: dao.insertShop(
            ShopEntity(
                name = name.trim(),
                ownerName = ownerName.trim(),
                phone = phone.trim(),
                address = address.trim(),
                notes = notes.trim(),
                createdAt = now().toStorage()
            )
        )
    }

    private suspend fun findOrCreatePerson(name: String, relationship: String, phone: String, notes: String): Long {
        val existing = dao.findPersonByName(name.trim())
        return existing?.id ?: dao.insertPerson(
            PersonEntity(
                name = name.trim(),
                relationship = relationship.trim(),
                phone = phone.trim(),
                notes = notes.trim()
            )
        )
    }
}

object BackupValidator {
    fun validate(payload: BackupPayload) {
        if (payload.backupVersion != 1 || payload.schemaVersion != 1) {
            throw BusinessRuleException("This backup version isn't supported.")
        }
        val shopIds = uniqueIds(payload.shops.map { it.id })
        val creditIds = uniqueIds(payload.shopCredits.map { it.id })
        val loanIds = uniqueIds(payload.loans.map { it.id })
        val loanInstallmentIds = uniqueIds(payload.loanInstallments.map { it.id })
        val emiIds = uniqueIds(payload.emiPurchases.map { it.id })
        val emiInstallmentIds = uniqueIds(payload.emiInstallments.map { it.id })
        val peopleIds = uniqueIds(payload.people.map { it.id })
        val debtIds = uniqueIds(payload.personalDebts.map { it.id })

        payload.userProfile?.createdAt?.validateDateTime()
        payload.shops.forEach { require(it.name.isNotBlank()); it.createdAt.validateDateTime() }
        payload.shopCredits.forEach { require(it.shopId in shopIds && it.totalAmountMinor >= 0L); it.purchaseDate.validateDate(); it.dueDate?.validateDate(); it.createdAt.validateDateTime() }
        payload.shopItems.forEach { require(it.creditId in creditIds && it.lineTotalMinor >= 0L && it.unitPriceMinor >= 0L) }
        payload.shopPayments.forEach { require(it.shopId in shopIds && it.amountMinor >= 0L); it.paidAt.validateDateTime() }
        payload.shopPaymentAllocations.forEach { require(it.creditId in creditIds && it.amountMinor >= 0L) }

        payload.loans.forEach {
            require(it.name.isNotBlank() && it.totalPayableMinor >= 0L && it.loanAmountMinor >= 0L && it.installmentAmountMinor >= 0L)
            it.dateTaken.validateDate(); it.firstDueDate.validateDate(); it.maturityDate.validateDate(); it.createdAt.validateDateTime()
        }
        payload.loanInstallments.forEach { require(it.loanId in loanIds && it.scheduledAmountMinor >= 0L && it.paidAmountMinor >= 0L); it.dueDate.validateDate() }
        payload.loanPayments.forEach { require(it.loanId in loanIds && it.amountMinor >= 0L); it.paidAt.validateDateTime() }
        payload.loanPaymentAllocations.forEach { require(it.installmentId in loanInstallmentIds && it.amountMinor >= 0L) }

        payload.emiPurchases.forEach {
            require(it.productName.isNotBlank() && it.totalPayableMinor >= 0L && it.totalPriceMinor >= 0L && it.installmentAmountMinor >= 0L)
            it.purchaseDate.validateDate(); it.firstDueDate.validateDate(); it.createdAt.validateDateTime()
        }
        payload.emiInstallments.forEach { require(it.emiId in emiIds && it.scheduledAmountMinor >= 0L && it.paidAmountMinor >= 0L); it.dueDate.validateDate() }
        payload.emiPayments.forEach { require(it.emiId in emiIds && it.amountMinor >= 0L); it.paidAt.validateDateTime() }
        payload.emiPaymentAllocations.forEach { require(it.installmentId in emiInstallmentIds && it.amountMinor >= 0L) }

        payload.people.forEach { require(it.name.isNotBlank()) }
        payload.personalDebts.forEach { require(it.personId in peopleIds && it.amountMinor >= 0L); it.startedOn.validateDate(); it.expectedDate?.validateDate(); it.createdAt.validateDateTime() }
        payload.personalSettlements.forEach { require(it.debtId in debtIds && it.amountMinor >= 0L); it.settledAt.validateDateTime() }
        payload.income.forEach { require(it.amountMinor >= 0L && it.source.isNotBlank()); it.occurredAt.validateDateTime() }
        payload.expenses.forEach { require(it.amountMinor >= 0L && it.category.isNotBlank()); it.occurredAt.validateDateTime() }
        payload.transactions.forEach { require(it.amountMinor >= 0L && it.title.isNotBlank()); it.occurredAt.validateDateTime() }
    }

    private fun uniqueIds(ids: List<Long>): Set<Long> {
        if (ids.any { it <= 0L } || ids.size != ids.toSet().size) throw BusinessRuleException("This backup file can't be read.")
        return ids.toSet()
    }

    private fun require(value: Boolean) {
        if (!value) throw BusinessRuleException("This backup file can't be read.")
    }

    private fun String.validateDate() {
        try {
            toLocalDate()
        } catch (_: Exception) {
            throw BusinessRuleException("This backup file can't be read.")
        }
    }

    private fun String.validateDateTime() {
        try {
            toLocalDateTime()
        } catch (_: Exception) {
            throw BusinessRuleException("This backup file can't be read.")
        }
    }
}

private fun FinancialTransactionEntity.toListItem() = TransactionListItem(
    id = id,
    title = title,
    subtitle = relatedName.ifBlank { category },
    amountMinor = amountMinor,
    cashEffect = cashEffect,
    type = type,
    statusLabel = statusLabel,
    occurredAt = occurredAt
)

private fun LoanInstallmentEntity.toDerived() = DerivedInstallment(
    id = id,
    number = installmentNumber,
    dueDate = dueDate,
    scheduledAmountMinor = scheduledAmountMinor,
    paidAmountMinor = paidAmountMinor,
    remainingAmountMinor = InstallmentStatusEngine.remainingAmount(scheduledAmountMinor, paidAmountMinor),
    status = InstallmentStatusEngine.derive(dueDate, scheduledAmountMinor, paidAmountMinor)
)

private fun EmiInstallmentEntity.toDerived() = DerivedInstallment(
    id = id,
    number = installmentNumber,
    dueDate = dueDate,
    scheduledAmountMinor = scheduledAmountMinor,
    paidAmountMinor = paidAmountMinor,
    remainingAmountMinor = InstallmentStatusEngine.remainingAmount(scheduledAmountMinor, paidAmountMinor),
    status = InstallmentStatusEngine.derive(dueDate, scheduledAmountMinor, paidAmountMinor)
)

private fun deriveCreditStatus(dueDate: String?, totalAmountMinor: Long, paidMinor: Long): PaymentStatus {
    if (paidMinor >= totalAmountMinor) return PaymentStatus.PAID
    if (dueDate == null) return if (paidMinor > 0L) PaymentStatus.PARTIALLY_PAID else PaymentStatus.UPCOMING
    return InstallmentStatusEngine.derive(dueDate, totalAmountMinor, paidMinor)
}

private fun deriveShopAccounts(
    shops: List<ShopEntity>,
    credits: List<ShopCreditEntity>,
    allocations: List<ShopPaymentAllocationEntity>
): List<AccountListItem> {
    return shops.filterNot { it.archived }.mapNotNull { shop ->
        val shopCredits = credits.filter { it.shopId == shop.id }
        if (shopCredits.isEmpty()) return@mapNotNull null
        val total = shopCredits.sumOf { it.totalAmountMinor }
        val paid = shopCredits.sumOf { credit -> allocations.filter { it.creditId == credit.id }.sumOf { it.amountMinor } }
        val remaining = maxOf(0L, total - paid)
        val activeCredits = shopCredits.map { credit ->
            val creditPaid = allocations.filter { it.creditId == credit.id }.sumOf { it.amountMinor }
            Triple(credit, creditPaid, deriveCreditStatus(credit.dueDate, credit.totalAmountMinor, creditPaid))
        }.filter { it.third != PaymentStatus.PAID }
        val status = when {
            activeCredits.any { it.third == PaymentStatus.OVERDUE } -> PaymentStatus.OVERDUE
            activeCredits.any { it.third == PaymentStatus.DUE_TODAY } -> PaymentStatus.DUE_TODAY
            activeCredits.any { it.third == PaymentStatus.PARTIALLY_PAID } -> PaymentStatus.PARTIALLY_PAID
            remaining == 0L -> PaymentStatus.PAID
            else -> PaymentStatus.UPCOMING
        }
        AccountListItem(
            id = shop.id,
            type = "SHOP",
            title = shop.name,
            subtitle = if (shop.ownerName.isBlank()) "Shop Credit" else shop.ownerName,
            totalAmountMinor = total,
            paidAmountMinor = paid,
            remainingAmountMinor = remaining,
            nextDueDate = activeCredits.mapNotNull { it.first.dueDate }.minOrNull(),
            status = status,
            progress = if (total == 0L) 0f else (paid.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        )
    }.sortedWith(compareBy<AccountListItem> { it.status != PaymentStatus.OVERDUE }.thenBy { it.nextDueDate ?: "9999-12-31" })
}

private fun deriveLoanAccounts(loans: List<LoanEntity>, installments: List<LoanInstallmentEntity>): List<AccountListItem> {
    return loans.filterNot { it.archived }.map { loan ->
        val loanInstallments = installments.filter { it.loanId == loan.id }.map { it.toDerived() }
        val totalPaid = loanInstallments.sumOf { it.paidAmountMinor }
        val remaining = maxOf(0L, loan.totalPayableMinor - totalPaid)
        val next = loanInstallments.firstOrNull { it.status != PaymentStatus.PAID }
        AccountListItem(
            id = loan.id,
            type = "LOAN",
            title = loan.name,
            subtitle = loan.institution,
            totalAmountMinor = loan.totalPayableMinor,
            paidAmountMinor = totalPaid,
            remainingAmountMinor = remaining,
            nextDueDate = next?.dueDate,
            status = next?.status ?: PaymentStatus.PAID,
            progress = if (loan.totalPayableMinor == 0L) 0f else (totalPaid.toFloat() / loan.totalPayableMinor.toFloat()).coerceIn(0f, 1f)
        )
    }.sortedBy { it.nextDueDate ?: "9999-12-31" }
}

private fun deriveEmiAccounts(emis: List<EmiPurchaseEntity>, installments: List<EmiInstallmentEntity>): List<AccountListItem> {
    return emis.filterNot { it.archived }.map { emi ->
        val emiInstallments = installments.filter { it.emiId == emi.id }.map { it.toDerived() }
        val totalPaid = emiInstallments.sumOf { it.paidAmountMinor }
        val remaining = maxOf(0L, emi.totalPayableMinor - totalPaid)
        val next = emiInstallments.firstOrNull { it.status != PaymentStatus.PAID }
        AccountListItem(
            id = emi.id,
            type = "EMI",
            title = emi.productName,
            subtitle = emi.provider,
            totalAmountMinor = emi.totalPayableMinor,
            paidAmountMinor = totalPaid,
            remainingAmountMinor = remaining,
            nextDueDate = next?.dueDate,
            status = next?.status ?: PaymentStatus.PAID,
            progress = if (emi.totalPayableMinor == 0L) 0f else (totalPaid.toFloat() / emi.totalPayableMinor.toFloat()).coerceIn(0f, 1f)
        )
    }.sortedBy { it.nextDueDate ?: "9999-12-31" }
}

private fun derivePersonalAccounts(
    people: List<PersonEntity>,
    debts: List<PersonalDebtEntity>,
    settlements: List<PersonalSettlementEntity>
): List<AccountListItem> {
    return debts.filterNot { it.archived }.mapNotNull { debt ->
        val person = people.firstOrNull { it.id == debt.personId } ?: return@mapNotNull null
        val settled = settlements.filter { it.debtId == debt.id }.sumOf { it.amountMinor }
        val remaining = maxOf(0L, debt.amountMinor - settled)
        val status = deriveCreditStatus(debt.expectedDate, debt.amountMinor, settled)
        AccountListItem(
            id = debt.id,
            type = if (debt.direction == PersonalDirection.BORROWED) "PERSONAL_BORROWED" else "PERSONAL_LENT",
            title = person.name,
            subtitle = person.relationship.ifBlank { if (debt.direction == PersonalDirection.BORROWED) "Borrowed" else "Lent" },
            totalAmountMinor = debt.amountMinor,
            paidAmountMinor = settled,
            remainingAmountMinor = remaining,
            nextDueDate = debt.expectedDate,
            status = status,
            progress = if (debt.amountMinor == 0L) 0f else (settled.toFloat() / debt.amountMinor.toFloat()).coerceIn(0f, 1f)
        )
    }.sortedBy { it.nextDueDate ?: "9999-12-31" }
}

private fun buildDueItems(
    shops: List<ShopEntity>,
    credits: List<ShopCreditEntity>,
    allocations: List<ShopPaymentAllocationEntity>,
    loans: List<LoanEntity>,
    loanInstallments: List<LoanInstallmentEntity>,
    emis: List<EmiPurchaseEntity>,
    emiInstallments: List<EmiInstallmentEntity>,
    people: List<PersonEntity>,
    personalDebts: List<PersonalDebtEntity>,
    personalSettlements: List<PersonalSettlementEntity>
): List<DueItem> {
    val items = mutableListOf<DueItem>()
    credits.forEach { credit ->
        val shop = shops.firstOrNull { it.id == credit.shopId } ?: return@forEach
        val paid = allocations.filter { it.creditId == credit.id }.sumOf { it.amountMinor }
        val remaining = maxOf(0L, credit.totalAmountMinor - paid)
        val dueDate = credit.dueDate ?: return@forEach
        val status = deriveCreditStatus(dueDate, credit.totalAmountMinor, paid)
        if (remaining > 0L) {
            items += DueItem("shop-${credit.id}", shop.name, "Shop Credit", remaining, dueDate, status, "SHOP", shop.id)
        }
    }
    loanInstallments.forEach { installment ->
        val loan = loans.firstOrNull { it.id == installment.loanId } ?: return@forEach
        val derived = installment.toDerived()
        if (derived.remainingAmountMinor > 0L) {
            items += DueItem("loan-${installment.id}", loan.name, loan.institution, derived.remainingAmountMinor, installment.dueDate, derived.status, "LOAN", loan.id)
        }
    }
    emiInstallments.forEach { installment ->
        val emi = emis.firstOrNull { it.id == installment.emiId } ?: return@forEach
        val derived = installment.toDerived()
        if (derived.remainingAmountMinor > 0L) {
            items += DueItem("emi-${installment.id}", emi.productName, emi.provider, derived.remainingAmountMinor, installment.dueDate, derived.status, "EMI", emi.id)
        }
    }
    personalDebts.filter { it.direction == PersonalDirection.BORROWED }.forEach { debt ->
        val person = people.firstOrNull { it.id == debt.personId } ?: return@forEach
        val settled = personalSettlements.filter { it.debtId == debt.id }.sumOf { it.amountMinor }
        val remaining = maxOf(0L, debt.amountMinor - settled)
        val dueDate = debt.expectedDate ?: return@forEach
        val status = deriveCreditStatus(dueDate, debt.amountMinor, settled)
        if (remaining > 0L) {
            items += DueItem("personal-${debt.id}", person.name, "Personal Debt", remaining, dueDate, status, "PERSONAL", debt.id)
        }
    }
    val windowEnd = today().plusDays(7)
    return items.filter {
        val date = it.dueDate.toLocalDate()
        it.status == PaymentStatus.OVERDUE || it.status == PaymentStatus.DUE_TODAY || (!date.isAfter(windowEnd) && it.status == PaymentStatus.UPCOMING)
    }.sortedBy { it.dueDate }
}

private fun buildMoneyOverview(transactions: List<FinancialTransactionEntity>): List<MoneyPoint> {
    val start = today().minusDays(6)
    return (0..6).map { offset ->
        val date = start.plusDays(offset.toLong()).toStorage()
        val day = transactions.filter { it.occurredAt.startsWith(date) }
        MoneyPoint(
            label = date.substring(5),
            incomeMinor = day.filter { it.type == TransactionType.INCOME }.sumOf { it.amountMinor },
            expenseMinor = day.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountMinor },
            paymentMinor = day.filter { it.type in KhataGoRepository.paymentTypes }.sumOf { it.amountMinor }
        )
    }
}

private val PaymentStatus.label: String
    get() = when (this) {
        PaymentStatus.UPCOMING -> "Upcoming"
        PaymentStatus.DUE_TODAY -> "Due Today"
        PaymentStatus.PARTIALLY_PAID -> "Partially Paid"
        PaymentStatus.PAID -> "Paid"
        PaymentStatus.OVERDUE -> "Overdue"
    }

private val ReportRange.label: String
    get() = when (this) {
        ReportRange.TODAY -> "Today"
        ReportRange.THIS_WEEK -> "This Week"
        ReportRange.THIS_MONTH -> "This Month"
        ReportRange.LAST_MONTH -> "Last Month"
        ReportRange.THIS_YEAR -> "This Year"
    }

private fun ReportRange.resolveDateRange(): Pair<LocalDate, LocalDate> {
    val today = today()
    return when (this) {
        ReportRange.TODAY -> today to today
        ReportRange.THIS_WEEK -> today.minusDays(today.dayOfWeek.value.toLong() - 1) to today.plusDays(7 - today.dayOfWeek.value.toLong())
        ReportRange.THIS_MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        ReportRange.LAST_MONTH -> {
            val lastMonth = today.minusMonths(1)
            lastMonth.withDayOfMonth(1) to lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
        }
        ReportRange.THIS_YEAR -> today.withDayOfYear(1) to today.withDayOfYear(today.lengthOfYear())
    }
}

private suspend fun <T> Flow<T>.firstValue(): T = first()

private suspend fun <T> Flow<T>.firstValue(): T = first()
