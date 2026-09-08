# KhataGo

**All your finances, in one place.**

KhataGo is an offline-first Android personal finance app built with Kotlin, Jetpack Compose, Room, Coroutines, Flow, DataStore, and WorkManager-oriented architecture.

## Current implementation status

This repository now contains a fresh KhataGo Android codebase created from scratch. The current build focuses on the core foundation:

- onboarding and setup flow
- light-only Compose design system
- local Room database schema for finance modules
- dashboard with real database-driven totals and empty states
- accounts overview
- account detail screens
- transaction history
- reports with local insights
- add flows for shop credit, loans, EMI, personal debt, income, expense, and payments
- JSON backup engine in the repository layer

Some specification items are **not fully finished yet**, including:

- official attached logo integration (the current repository uses a temporary technical placeholder mark because no official asset was available in this Arena session)
- SAF-connected backup/restore UI
- CSV export UI flow
- PDF export UI flow
- notification workflow wiring
- app lock and biometric gate
- full edit/delete/archive flows
- migration evolution beyond schema version 1
- release signing and GitHub release verification

## Features in progress

### Implemented foundation
- Shop Credit
- Loans
- EMI
- Personal Debt
- Income
- Expenses
- Payments
- Dashboard
- Reports
- Analytics-style overview cards
- Search
- Backup serialization engine

### Planned next milestones
- Restore UI validation flow
- CSV export
- PDF report generation
- notification reminders
- app lock
- edit/delete/archive actions
- CI verification and release packaging

## Architecture

- **Presentation:** Jetpack Compose screens and reusable design components
- **Data:** Room entities and DAO, local-first repository, DataStore preferences
- **Domain:** deterministic financial engines for schedules, status derivation, insights, and money formatting

See:
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [DATABASE.md](DATABASE.md)
- [FINANCIAL_LOGIC.md](FINANCIAL_LOGIC.md)
- [PRIVACY.md](PRIVACY.md)
- [RELEASE.md](RELEASE.md)

## Privacy

KhataGo is designed to be:

- Offline-first
- Local-first
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
```

GitHub Actions workflows are included under `.github/workflows/`.

## Testing

Unit tests cover:

- money precision formatting and parsing
- installment schedule generation
- due and overdue status derivation
- insight rules
- backup validation rules

## Developer

Created by **Shohan Khan**  
Contact: **helloiamshohan@gmail.com**
