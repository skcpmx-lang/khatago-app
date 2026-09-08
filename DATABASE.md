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

## Notes on integrity

- foreign keys are enabled through Room entity declarations
- payment allocations are stored explicitly for shop credit, loan, and EMI payments
- personal debt settlements attach directly to a single debt record
- dashboard and reports are derived from source records rather than duplicated balance fields

## Migration status

- current project is at initial schema version `1`
- no destructive migration fallback is used
- future schema changes must add explicit Room migrations
