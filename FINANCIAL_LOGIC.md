# Financial Logic

## Money precision

All financial amounts are stored as `Long` minor units.

Example:
- `৳100.50` -> `10050`

KhataGo does not use `Float` or `Double` for money.

## Cash flow semantics

### Counted as income
- `INCOME`
- `PERSONAL_RETURN` increases cash on hand but is **not** treated as income in current reports unless specifically added as income

### Counted as expense
- `EXPENSE`

### Counted as payments
- `SHOP_PAYMENT`
- `LOAN_PAYMENT`
- `EMI_PAYMENT`
- `PERSONAL_REPAYMENT`

### Neutral obligation creation
These create liabilities or receivables but do not directly change cash flow in current logic:
- `SHOP_CREDIT`
- `LOAN`
- `EMI_PURCHASE`
- `PERSONAL_BORROWING`
- `PERSONAL_LENDING`

## Outstanding totals

Current total outstanding is calculated as:

- shop credit remaining
- loan remaining
- EMI remaining
- personal borrowed remaining

Personal lent money is tracked separately and is not included in outstanding debt.

## Overpayment protection

Before saving a payment, KhataGo checks the remaining due amount.

If a new payment is larger than the amount due, the operation fails with:

> This payment is higher than the amount due.

## Installment status rules

Derived rules currently implemented:

- paid >= scheduled -> Paid
- paid > 0 and paid < scheduled and now > due date -> Overdue
- paid > 0 and paid < scheduled -> Partially Paid
- paid == 0 and today == due date -> Due Today
- paid == 0 and today > due date -> Overdue
- otherwise -> Upcoming

## Schedule generation

- weekly schedules use `plusWeeks`
- monthly schedules use `plusMonths`
- month-end and leap-year handling rely on `java.time`
- the final installment receives any remaining rounding difference
