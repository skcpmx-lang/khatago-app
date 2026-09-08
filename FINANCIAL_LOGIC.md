# Financial Logic

## Money precision
All money values are stored as `Long` minor units.

Example:
- `৳100.50` -> `10050`

KhataGo does not use `Float` or `Double` for money calculations.

## Transaction semantics
### Income in reports
- `INCOME`

### Expense in reports
- `EXPENSE`

### Payments tracked separately
- `SHOP_PAYMENT`
- `LOAN_PAYMENT`
- `EMI_PAYMENT`
- `PERSONAL_REPAYMENT`
- `PERSONAL_RETURN`

### Neutral obligation creation events
These create obligations or receivables, but they are not automatically treated as income or cash expense in current report totals:
- `SHOP_CREDIT`
- `LOAN`
- `EMI_PURCHASE`
- `PERSONAL_BORROWING`
- `PERSONAL_LENDING`

## Outstanding totals
Current total outstanding debt includes:
- shop credit remaining
- loan remaining
- EMI remaining
- personal borrowed remaining

Personal lent money is tracked separately and is not counted as debt owed by the user.

## Overpayment protection
Before a payment is saved, KhataGo checks the actual remaining amount.

If a payment is larger than the amount due, the operation fails with:

> This payment is higher than the amount due.

## Installment rules
Current derived rules:
- paid >= scheduled -> Paid
- paid > 0 and paid < scheduled and current date > due date -> Overdue
- paid > 0 and paid < scheduled -> Partially Paid
- paid == 0 and current date == due date -> Due Today
- paid == 0 and current date > due date -> Overdue
- otherwise -> Upcoming

A fully paid installment never remains overdue.

## Schedule generation
- weekly schedules use `plusWeeks`
- monthly schedules use `plusMonths`
- Java time handles month-end, leap years, and year transitions
- the final installment receives any remainder from division so totals stay exact

## Payment allocation
### Shop payments
Shop payments are allocated oldest-first across outstanding shop credit purchases.

### Loan payments
Loan payments are allocated installment-by-installment in schedule order.

### EMI payments
EMI payments are allocated installment-by-installment in schedule order.

### Personal debt settlements
Each settlement attaches directly to one personal debt record.
