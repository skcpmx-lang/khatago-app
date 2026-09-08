# Architecture

KhataGo currently uses a single Android app module with a clean separation of concerns inside the module.

## Layers

### Presentation
- Jetpack Compose
- Material 3 light-only theme
- screen-level state collected from Flow using lifecycle-aware APIs
- reusable KhataGo design components for hero cards, metric cards, action tiles, rows, status badges, and chart cards

### Domain
- `Money` for minor-unit parsing and formatting
- `InstallmentScheduleGenerator` for weekly/monthly schedules
- `InstallmentStatusEngine` for derived payment status
- `InsightEngine` for deterministic local insights

### Data
- Room as the source of truth
- normalized entities for accounts, schedules, payments, allocations, cash entries, categories, and transaction history
- `KhataGoRepository` as the central orchestration layer for:
  - creating records
  - allocating payments
  - preventing overpayment
  - deriving dashboard data
  - deriving reports
  - serializing backups

### Preferences
- DataStore Preferences for onboarding and app-level toggles

## Current limitations

The current implementation does not yet include:
- dependency-injection framework
- migration history beyond version 1
- full edit/delete/archive flows
- completed export pipelines
- completed security gate
- completed reminder scheduling integration

## Theme policy

KhataGo forces light mode and does not follow system dark mode.
