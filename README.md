# KhataGo

**All your finances, in one place.**

KhataGo is a local-first Android personal finance app built with Kotlin, Jetpack Compose, Room, Coroutines, Flow, DataStore, and WorkManager-oriented scheduling.

## Current implementation snapshot

This repository contains a fresh KhataGo codebase created from scratch on branch `arena/01a07f49-khatago-app`.

### Implemented now
- onboarding and setup flow
- light-only Compose design system
- Room database schema for shops, loans, EMI, personal debt, income, expense, and transactions
- dashboard with real database-driven balances, due items, recent activity, and six quick actions
- accounts overview with tabs
- account detail screens for shop credit, loans, EMI, and personal debt
- transaction history with income/expense deletion flow
- reports with local rule-based insights
- add flows for shop credit, loans, EMI, personal debt, income, expense, and payments
- JSON backup export and restore validation flow
- CSV export flow
- PDF export flow
- reminder scheduling foundation with WorkManager and notifications
- PIN setup and biometric unlock foundation

### Still incomplete
- exact official binary logo asset integration from the attached file path was not possible in the sandbox because the referenced upload files were not present on disk
- edit flows for every entity are not complete yet
- delete/edit coverage for every payment and child record is not complete yet
- reminder UX and timing are basic
- runtime verification, APK verification, and release verification are still pending

## Features
- Shop Credit
- Loans
- EMI
- Personal Debt
- Income
- Expenses
- Payments
- Dashboard
- Reports
- Search
- Backup
- Restore
- CSV export
- PDF export
- Notifications foundation
- App lock foundation

## Architecture
- **Presentation:** Jetpack Compose + Material 3
- **Data:** Room + repository layer + DataStore
- **Domain:** deterministic money, schedule, status, and insight engines
- **Background:** WorkManager reminder scheduling

See:
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [DATABASE.md](DATABASE.md)
- [FINANCIAL_LOGIC.md](FINANCIAL_LOGIC.md)
- [PRIVACY.md](PRIVACY.md)
- [RELEASE.md](RELEASE.md)

## Privacy
KhataGo is designed to be:
- Offline-first
- No ads
- No subscription
- No paid APIs
- No paid SDKs
- No tracking
- No external financial backend

## Build
Local Android build tooling was **not available** in this Arena environment.

When Java + Android tooling are available:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
```

## Testing
Unit tests currently cover:
- money parsing and formatting
- schedule generation
- installment status rules
- cash flow calculation
- insight generation
- backup validation rules
- CSV escaping
- PIN hashing verification

## Developer
Created by **Shohan Khan**  
Contact: **helloiamshohan@gmail.com**
