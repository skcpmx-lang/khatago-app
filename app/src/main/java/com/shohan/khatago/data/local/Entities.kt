package com.shohan.khatago.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
enum class Frequency { WEEKLY, MONTHLY }

@Serializable
enum class PersonalDirection { BORROWED, LENT }

@Serializable
enum class TransactionType {
    SHOP_CREDIT,
    SHOP_PAYMENT,
    LOAN,
    LOAN_PAYMENT,
    EMI_PURCHASE,
    EMI_PAYMENT,
    PERSONAL_BORROWING,
    PERSONAL_REPAYMENT,
    PERSONAL_LENDING,
    PERSONAL_RETURN,
    INCOME,
    EXPENSE
}

@Serializable
enum class TransactionCashEffect { IN, OUT, NEUTRAL }

@Serializable
@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val createdAt: String
)

@Serializable
@Entity(tableName = "shops", indices = [Index("name")])
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val ownerName: String,
    val phone: String,
    val address: String,
    val notes: String,
    val archived: Boolean = false,
    val createdAt: String
)

@Serializable
@Entity(
    tableName = "shop_credits",
    foreignKeys = [ForeignKey(
        entity = ShopEntity::class,
        parentColumns = ["id"],
        childColumns = ["shopId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("shopId"), Index("purchaseDate"), Index("dueDate")]
)
data class ShopCreditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val purchaseDate: String,
    val dueDate: String?,
    val totalAmountMinor: Long,
    val notes: String,
    val createdAt: String
)

@Serializable
@Entity(
    tableName = "shop_credit_items",
    foreignKeys = [ForeignKey(
        entity = ShopCreditEntity::class,
        parentColumns = ["id"],
        childColumns = ["creditId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("creditId")]
)
data class ShopCreditItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditId: Long,
    val itemName: String,
    val quantityText: String,
    val unit: String,
    val unitPriceMinor: Long,
    val lineTotalMinor: Long
)

@Serializable
@Entity(
    tableName = "shop_payments",
    foreignKeys = [ForeignKey(
        entity = ShopEntity::class,
        parentColumns = ["id"],
        childColumns = ["shopId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("shopId"), Index("paidAt")]
)
data class ShopPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val paidAt: String,
    val amountMinor: Long,
    val method: String,
    val notes: String
)

@Serializable
@Entity(
    tableName = "shop_payment_allocations",
    foreignKeys = [
        ForeignKey(
            entity = ShopPaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ShopCreditEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paymentId"), Index("creditId")]
)
data class ShopPaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentId: Long,
    val creditId: Long,
    val amountMinor: Long
)

@Serializable
@Entity(tableName = "loans", indices = [Index("name"), Index("institution")])
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val institution: String,
    val loanAmountMinor: Long,
    val dateTaken: String,
    val interestRate: String,
    val processingFeeMinor: Long,
    val totalPayableMinor: Long,
    val installmentAmountMinor: Long,
    val frequency: Frequency,
    val installmentCount: Int,
    val firstDueDate: String,
    val maturityDate: String,
    val notes: String,
    val createdAt: String,
    val archived: Boolean = false
)

@Serializable
@Entity(
    tableName = "loan_installments",
    foreignKeys = [ForeignKey(
        entity = LoanEntity::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loanId"), Index("dueDate")]
)
data class LoanInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val installmentNumber: Int,
    val dueDate: String,
    val scheduledAmountMinor: Long,
    val paidAmountMinor: Long = 0
)

@Serializable
@Entity(
    tableName = "loan_payments",
    foreignKeys = [ForeignKey(
        entity = LoanEntity::class,
        parentColumns = ["id"],
        childColumns = ["loanId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("loanId"), Index("paidAt")]
)
data class LoanPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val loanId: Long,
    val paidAt: String,
    val amountMinor: Long,
    val method: String,
    val notes: String
)

@Serializable
@Entity(
    tableName = "loan_payment_allocations",
    foreignKeys = [
        ForeignKey(
            entity = LoanPaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LoanInstallmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["installmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paymentId"), Index("installmentId")]
)
data class LoanPaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentId: Long,
    val installmentId: Long,
    val amountMinor: Long
)

@Serializable
@Entity(tableName = "emi_purchases", indices = [Index("productName"), Index("provider")])
data class EmiPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
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
    val notes: String,
    val createdAt: String,
    val archived: Boolean = false
)

@Serializable
@Entity(
    tableName = "emi_installments",
    foreignKeys = [ForeignKey(
        entity = EmiPurchaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["emiId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("emiId"), Index("dueDate")]
)
data class EmiInstallmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emiId: Long,
    val installmentNumber: Int,
    val dueDate: String,
    val scheduledAmountMinor: Long,
    val paidAmountMinor: Long = 0
)

@Serializable
@Entity(
    tableName = "emi_payments",
    foreignKeys = [ForeignKey(
        entity = EmiPurchaseEntity::class,
        parentColumns = ["id"],
        childColumns = ["emiId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("emiId"), Index("paidAt")]
)
data class EmiPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val emiId: Long,
    val paidAt: String,
    val amountMinor: Long,
    val method: String,
    val notes: String
)

@Serializable
@Entity(
    tableName = "emi_payment_allocations",
    foreignKeys = [
        ForeignKey(
            entity = EmiPaymentEntity::class,
            parentColumns = ["id"],
            childColumns = ["paymentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmiInstallmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["installmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("paymentId"), Index("installmentId")]
)
data class EmiPaymentAllocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val paymentId: Long,
    val installmentId: Long,
    val amountMinor: Long
)

@Serializable
@Entity(tableName = "people", indices = [Index("name")])
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val relationship: String,
    val phone: String,
    val notes: String
)

@Serializable
@Entity(
    tableName = "personal_debts",
    foreignKeys = [ForeignKey(
        entity = PersonEntity::class,
        parentColumns = ["id"],
        childColumns = ["personId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("personId"), Index("expectedDate")]
)
data class PersonalDebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personId: Long,
    val direction: PersonalDirection,
    val amountMinor: Long,
    val startedOn: String,
    val expectedDate: String?,
    val notes: String,
    val createdAt: String,
    val archived: Boolean = false
)

@Serializable
@Entity(
    tableName = "personal_settlements",
    foreignKeys = [ForeignKey(
        entity = PersonalDebtEntity::class,
        parentColumns = ["id"],
        childColumns = ["debtId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("debtId"), Index("settledAt")]
)
data class PersonalSettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val settledAt: String,
    val amountMinor: Long,
    val notes: String
)

@Serializable
@Entity(tableName = "custom_categories", indices = [Index(value = ["kind", "name"], unique = true)])
data class CustomCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val name: String
)

@Serializable
@Entity(tableName = "income", indices = [Index("occurredAt"), Index("category")])
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAt: String,
    val amountMinor: Long,
    val source: String,
    val category: String,
    val notes: String
)

@Serializable
@Entity(tableName = "expenses", indices = [Index("occurredAt"), Index("category")])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAt: String,
    val amountMinor: Long,
    val category: String,
    val place: String,
    val notes: String
)

@Serializable
@Entity(tableName = "financial_transactions", indices = [Index("occurredAt"), Index("title"), Index("type")])
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val occurredAt: String,
    val title: String,
    val category: String,
    val amountMinor: Long,
    val type: TransactionType,
    val relatedName: String,
    val relatedEntityType: String,
    val relatedEntityId: Long,
    val cashEffect: TransactionCashEffect,
    val statusLabel: String,
    val notes: String
)
