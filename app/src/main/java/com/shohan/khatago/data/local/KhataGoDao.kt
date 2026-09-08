package com.shohan.khatago.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface KhataGoDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeUserProfile(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsertUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM shops ORDER BY name ASC")
    fun observeShops(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shop_credits ORDER BY purchaseDate DESC, id DESC")
    fun observeShopCredits(): Flow<List<ShopCreditEntity>>

    @Query("SELECT * FROM shop_credit_items ORDER BY id ASC")
    fun observeShopCreditItems(): Flow<List<ShopCreditItemEntity>>

    @Query("SELECT * FROM shop_payments ORDER BY paidAt DESC, id DESC")
    fun observeShopPayments(): Flow<List<ShopPaymentEntity>>

    @Query("SELECT * FROM shop_payment_allocations")
    fun observeShopPaymentAllocations(): Flow<List<ShopPaymentAllocationEntity>>

    @Insert
    suspend fun insertShop(shop: ShopEntity): Long

    @Insert
    suspend fun insertShopCredit(credit: ShopCreditEntity): Long

    @Insert
    suspend fun insertShopCreditItems(items: List<ShopCreditItemEntity>)

    @Insert
    suspend fun insertShopPayment(payment: ShopPaymentEntity): Long

    @Insert
    suspend fun insertShopPaymentAllocations(items: List<ShopPaymentAllocationEntity>)

    @Update
    suspend fun updateShop(shop: ShopEntity)

    @Query("SELECT * FROM loans ORDER BY firstDueDate ASC, id DESC")
    fun observeLoans(): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loan_installments ORDER BY dueDate ASC, installmentNumber ASC")
    fun observeLoanInstallments(): Flow<List<LoanInstallmentEntity>>

    @Query("SELECT * FROM loan_payments ORDER BY paidAt DESC, id DESC")
    fun observeLoanPayments(): Flow<List<LoanPaymentEntity>>

    @Query("SELECT * FROM loan_payment_allocations")
    fun observeLoanPaymentAllocations(): Flow<List<LoanPaymentAllocationEntity>>

    @Insert
    suspend fun insertLoan(loan: LoanEntity): Long

    @Insert
    suspend fun insertLoanInstallments(items: List<LoanInstallmentEntity>)

    @Insert
    suspend fun insertLoanPayment(payment: LoanPaymentEntity): Long

    @Insert
    suspend fun insertLoanPaymentAllocations(items: List<LoanPaymentAllocationEntity>)

    @Update
    suspend fun updateLoanInstallments(items: List<LoanInstallmentEntity>)

    @Query("SELECT * FROM emi_purchases ORDER BY firstDueDate ASC, id DESC")
    fun observeEmiPurchases(): Flow<List<EmiPurchaseEntity>>

    @Query("SELECT * FROM emi_installments ORDER BY dueDate ASC, installmentNumber ASC")
    fun observeEmiInstallments(): Flow<List<EmiInstallmentEntity>>

    @Query("SELECT * FROM emi_payments ORDER BY paidAt DESC, id DESC")
    fun observeEmiPayments(): Flow<List<EmiPaymentEntity>>

    @Query("SELECT * FROM emi_payment_allocations")
    fun observeEmiPaymentAllocations(): Flow<List<EmiPaymentAllocationEntity>>

    @Insert
    suspend fun insertEmiPurchase(emi: EmiPurchaseEntity): Long

    @Insert
    suspend fun insertEmiInstallments(items: List<EmiInstallmentEntity>)

    @Insert
    suspend fun insertEmiPayment(payment: EmiPaymentEntity): Long

    @Insert
    suspend fun insertEmiPaymentAllocations(items: List<EmiPaymentAllocationEntity>)

    @Update
    suspend fun updateEmiInstallments(items: List<EmiInstallmentEntity>)

    @Query("SELECT * FROM people ORDER BY name ASC")
    fun observePeople(): Flow<List<PersonEntity>>

    @Query("SELECT * FROM personal_debts ORDER BY expectedDate ASC, id DESC")
    fun observePersonalDebts(): Flow<List<PersonalDebtEntity>>

    @Query("SELECT * FROM personal_settlements ORDER BY settledAt DESC, id DESC")
    fun observePersonalSettlements(): Flow<List<PersonalSettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPerson(person: PersonEntity): Long

    @Query("SELECT * FROM people WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun findPersonByName(name: String): PersonEntity?

    @Insert
    suspend fun insertPersonalDebt(debt: PersonalDebtEntity): Long

    @Insert
    suspend fun insertPersonalSettlement(settlement: PersonalSettlementEntity): Long

    @Query("SELECT * FROM income ORDER BY occurredAt DESC, id DESC")
    fun observeIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM expenses ORDER BY occurredAt DESC, id DESC")
    fun observeExpenses(): Flow<List<ExpenseEntity>>

    @Insert
    suspend fun insertIncome(item: IncomeEntity): Long

    @Insert
    suspend fun insertExpense(item: ExpenseEntity): Long

    @Query("SELECT * FROM custom_categories ORDER BY kind ASC, name ASC")
    fun observeCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomCategory(category: CustomCategoryEntity)

    @Query("SELECT * FROM financial_transactions ORDER BY occurredAt DESC, id DESC")
    fun observeTransactions(): Flow<List<FinancialTransactionEntity>>

    @Insert
    suspend fun insertTransaction(transaction: FinancialTransactionEntity): Long

    @Insert
    suspend fun insertTransactions(transactions: List<FinancialTransactionEntity>)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()

    @Query("DELETE FROM financial_transactions")
    suspend fun clearTransactions()

    @Query("DELETE FROM shop_payment_allocations")
    suspend fun clearShopPaymentAllocations()

    @Query("DELETE FROM shop_payments")
    suspend fun clearShopPayments()

    @Query("DELETE FROM shop_credit_items")
    suspend fun clearShopCreditItems()

    @Query("DELETE FROM shop_credits")
    suspend fun clearShopCredits()

    @Query("DELETE FROM shops")
    suspend fun clearShops()

    @Query("DELETE FROM loan_payment_allocations")
    suspend fun clearLoanPaymentAllocations()

    @Query("DELETE FROM loan_payments")
    suspend fun clearLoanPayments()

    @Query("DELETE FROM loan_installments")
    suspend fun clearLoanInstallments()

    @Query("DELETE FROM loans")
    suspend fun clearLoans()

    @Query("DELETE FROM emi_payment_allocations")
    suspend fun clearEmiPaymentAllocations()

    @Query("DELETE FROM emi_payments")
    suspend fun clearEmiPayments()

    @Query("DELETE FROM emi_installments")
    suspend fun clearEmiInstallments()

    @Query("DELETE FROM emi_purchases")
    suspend fun clearEmiPurchases()

    @Query("DELETE FROM personal_settlements")
    suspend fun clearPersonalSettlements()

    @Query("DELETE FROM personal_debts")
    suspend fun clearPersonalDebts()

    @Query("DELETE FROM people")
    suspend fun clearPeople()

    @Query("DELETE FROM income")
    suspend fun clearIncome()

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM custom_categories")
    suspend fun clearCustomCategories()
}
