# Panini FIFA 2026 — Support Tickets PoC

Internal support ticket management application for Panini's FIFA World Cup 2026 album distribution operations.

## Overview

A proof-of-concept Android app that centralizes support ticket management for supplier issues, distribution problems, inventory shortages, and logistics incidents. Replaces informal workflows (emails, spreadsheets) with a structured, trackable system.

## Screens

| Screen | Description |
|--------|-------------|
| Login | Simulated authentication with credential validation |
| Ticket List | LazyColumn of all tickets, sorted by priority, auto-updating via event bus |
| Ticket Detail | Full ticket info with status and priority update actions |
| Create Ticket | Form to register new support tickets |

**Demo credentials:** `admin / admin123` or `operador / op2026`

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Jetpack Compose | Declarative UI |
| MVVM | Architecture pattern |
| Navigation Compose | Screen navigation |
| Kotlin Coroutines + Flow | Async operations and reactive state |
| SharedFlow (EventBus) | Cross-screen event communication |
| Retrofit + OkHttp | Networking layer (mock-intercepted for PoC) |
| Gson | JSON serialization |
| Material3 | UI components and theming |

## Architecture

```
UI (Screens) → ViewModels → Repository → Retrofit → MockInterceptor
                    ↕
              TicketEventBus (SharedFlow)
```

See [docs/architecture.md](docs/architecture.md) for full technical documentation.

## API Contracts

Defined in [contracts/tickets-api.yaml](contracts/tickets-api.yaml) — OpenAPI 3.0 spec covering all endpoints the app will consume when a real backend is available.

## Feature Flags

Controlled via `FeatureFlags.kt`:

- `isTicketCreationEnabled` — show/hide ticket creation UI
- `isPriorityUpdateEnabled` — show/hide priority update action
- `isDistributionCategoryVisible` — filter distribution category from selector
- `isAdminActionsVisible` — reserved for future admin features

## Running the App

1. Open the project in Android Studio (Ladybug or newer)
2. Sync Gradle dependencies
3. Run on an emulator or physical device (API 24+)
4. No backend required — all data is served by `MockTicketInterceptor`

## Project Structure

```
/app        Android project (Jetpack Compose, MVVM)
/contracts  API contracts in OpenAPI 3.0 YAML format
/docs       Technical documentation and architectural decisions
/video      Demo video link
README.md   This file
```

## Notes for Future Developers

- To connect a real backend: remove `MockTicketInterceptor` from `NetworkModule`, update `BASE_URL`
- To add DI: annotate ViewModels with `@HiltViewModel`, inject `TicketRepository`
- To add remote feature flags: replace `FeatureFlags` field values with remote config reads
- All consuming code remains unchanged in both cases — only the configuration layer changes
