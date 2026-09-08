# Architecture

KhataGo currently uses one Android app module with a clear split between UI, domain logic, persistence, preferences, export helpers, and security helpers.

## Layers

### Presentation
- Jetpack Compose
- Material 3 light-only theme
- lifecycle-aware Flow collection using `collectAsStateWithLifecycle`
- reusable KhataGo components for hero cards, metric cards, status badges, chart cards, account rows, transaction rows, and empty states
- navigation for onboarding, setup, home tabs, search, and account details

### Domain
- `Money` for deterministic minor-unit parsing and formatting
- `InstallmentScheduleGenerator` for weekly and monthly schedules
- `InstallmentStatusEngine` for derived installment state
- `InsightEngine` for local rule-based insights

### Data
- Room as the source of truth
- normalized entities for accounts, installments, payments, allocations, income, expenses, categories, and transaction history
- `KhataGoRepository` handles:
  - account creation
  - schedule generation
  - payment allocation
  - overpayment protection
  - dashboard/report derivation
  - backup serialization and restore validation
  - CSV export snapshots
  - archive and delete operations for top-level accounts

### Preferences and security
- DataStore Preferences for onboarding, reminder settings, and app lock settings
- hashed PIN storage with salt
- biometric unlock integration via `BiometricPrompt`

### Background work
- WorkManager periodic worker for payment reminder summaries

## Current limitations
The current implementation still needs more work for:
- full edit coverage across every entity
- full delete/edit support for every child record and payment record
- richer reminder scheduling windows
- broader migration coverage after schema version 1
- final asset integration from the unavailable upload file path
- release packaging verification
