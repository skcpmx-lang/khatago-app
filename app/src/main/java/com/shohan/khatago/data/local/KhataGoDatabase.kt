package com.shohan.khatago.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserProfileEntity::class,
        ShopEntity::class,
        ShopCreditEntity::class,
        ShopCreditItemEntity::class,
        ShopPaymentEntity::class,
        ShopPaymentAllocationEntity::class,
        LoanEntity::class,
        LoanInstallmentEntity::class,
        LoanPaymentEntity::class,
        LoanPaymentAllocationEntity::class,
        EmiPurchaseEntity::class,
        EmiInstallmentEntity::class,
        EmiPaymentEntity::class,
        EmiPaymentAllocationEntity::class,
        PersonEntity::class,
        PersonalDebtEntity::class,
        PersonalSettlementEntity::class,
        CustomCategoryEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        FinancialTransactionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class KhataGoDatabase : RoomDatabase() {
    abstract fun dao(): KhataGoDao

    companion object {
        fun create(context: Context): KhataGoDatabase = Room.databaseBuilder(
            context,
            KhataGoDatabase::class.java,
            "khatago.db"
        ).build()
    }
}
