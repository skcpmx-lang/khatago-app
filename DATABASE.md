# Database

KhataGo uses Room with schema version `1`.

## Implemented entities
- `UserProfileEntity`
- `ShopEntity`
- `ShopCreditEntity`
- `ShopCreditItemEntity`
- `ShopPaymentEntity`
- `ShopPaymentAllocationEntity`
- `LoanEntity`
- `LoanInstallmentEntity`
- `LoanPaymentEntity`
- `LoanPaymentAllocationEntity`
- `EmiPurchaseEntity`
- `EmiInstallmentEntity`
- `EmiPaymentEntity`
- `EmiPaymentAllocationEntity`
- `PersonEntity`
- `PersonalDebtEntity`
- `PersonalSettlementEntity`
- `CustomCategoryEntity`
- `IncomeEntity`
- `ExpenseEntity`
- `FinancialTransactionEntity`

## Integrity rules implemented
- foreign keys connect parent accounts to child schedules, items, payments, and allocations
- indexes exist on major search/date columns
- payment allocations are stored explicitly for shop credit, loan, and EMI payments
- dashboard and reports derive balances from source records instead of storing duplicated totals
- top-level account archive flags exist for shops, loans, EMI purchases, and personal debt

## Migration status
- current schema version: `1`
- `fallbackToDestructiveMigration()` is not used
- future schema changes must add explicit migrations before release verification
