# Technical Documentation — Panini FIFA 2026 Support Tickets PoC

## Architecture Overview

The app follows **MVVM (Model-View-ViewModel)** with a clean layered structure. Each layer has a single responsibility and knows only about the layer below it.

```
UI Layer (Screens + ViewModels)
        ↓
Data Layer (Repository)
        ↓
Network Layer (Retrofit + Mock Interceptor)
        ↓
Domain Layer (Models)
```

Cross-cutting concerns (EventBus, FeatureFlags, UiState) live in a `core` package accessible by all layers.

---

## Package Structure

```
com.example.examen2/
├── core/
│   ├── events/         TicketEvent.kt, TicketEventBus.kt
│   ├── flags/          FeatureFlags.kt
│   └── state/          UiState.kt
├── data/
│   ├── dto/            Dtos.kt (all request/response DTOs)
│   ├── mock/           MockTicketInterceptor.kt
│   ├── remote/         TicketApiService.kt, NetworkModule.kt
│   └── repository/     TicketRepository.kt
├── domain/
│   └── model/          Ticket.kt (+ enums)
└── ui/
    ├── navigation/     Screen.kt, AppNavGraph.kt
    ├── screen/
    │   ├── login/
    │   ├── ticketlist/
    │   ├── ticketdetail/
    │   └── createticket/
    └── theme/
```

---

## Event-Based Communication

**Why:** The ticket list must react to changes (new ticket, priority update, status change) that originate in other screens, without requiring a manual reload or sharing a ViewModel instance.

**How it works:** A singleton `TicketEventBus` wraps a Kotlin `SharedFlow`. Any ViewModel that performs a mutation publishes an event after a successful API call. The `TicketListViewModel` subscribes to these events in `init` and updates its in-memory list reactively.

```
CreateTicketViewModel                TicketListViewModel
      │                                      │
      │ repository.createTicket(...)         │ init { observeEvents() }
      │ → success                            │
      │                                      │
      └──► TicketEventBus.publish(           │
               TicketEvent.TicketCreated)    │
                        │                    │
                        └───────────────────►│ addTicketAndSort(ticket)
                                             │ _uiState.value = Success(sorted)
                                             │ → UI recomposes automatically
```

**Scenario 1 — Ticket created:** `CreateTicketScreen` → `CreateTicketViewModel.createTicket()` → repository call → on success, `TicketEventBus.publish(TicketCreated)` → `TicketListViewModel.addTicketAndSort()` → list recomposes.

**Scenario 2 — Priority updated:** `TicketDetailScreen` → `TicketDetailViewModel.updatePriority()` → repository call → on success, `TicketEventBus.publish(PriorityUpdated)` → `TicketListViewModel.updatePriorityAndSort()` → list reorders with higher priority tickets first.

**Why SharedFlow over LiveData or callbacks:**
- Coroutine-native, no Android lifecycle coupling
- Multiple collectors possible (scalable for future screens)
- `tryEmit` with buffer avoids suspension in non-coroutine callers
- No memory leaks — collected inside `viewModelScope`

---

## Feature Flags

**Why:** The PoC needs to allow enabling/disabling features quickly during internal testing without modifying multiple files or redeploying.

**Implementation:** A plain `object` with `var` Boolean properties. Simple, zero dependencies, easy to extend.

```kotlin
object FeatureFlags {
    var isTicketCreationEnabled: Boolean = true
    var isPriorityUpdateEnabled: Boolean = true
    var isAdminActionsVisible: Boolean = false
    var isDistributionCategoryVisible: Boolean = true
}
```

**How flags are used:**

| Flag | Effect |
|------|--------|
| `isTicketCreationEnabled` | Hides FAB in ticket list; blocks submission in CreateTicketScreen |
| `isPriorityUpdateEnabled` | Hides "Update Priority" button in ticket detail; guards ViewModel call |
| `isAdminActionsVisible` | Reserved for future admin-only UI sections |
| `isDistributionCategoryVisible` | Filters DISTRIBUTION from category selector in CreateTicketScreen |

**Extending to a remote config service:** When the team integrates a service like Firebase Remote Config, the `FeatureFlags` object becomes the single point of change — all consuming code remains untouched.

---

## Networking Layer

**Current state (PoC):** All HTTP calls are intercepted by `MockTicketInterceptor` before reaching the network. The interceptor parses URL patterns and returns realistic mock JSON responses.

**Why Retrofit even with mock data:** The full networking contract (`TicketApiService`, `NetworkModule`, DTOs) is already wired. Swapping to a real backend requires only:
1. Remove `MockTicketInterceptor` from `OkHttpClient`
2. Update `BASE_URL` in `NetworkModule`

No changes to ViewModels, Repository, or screens.

**Flow:** `ViewModel → Repository → TicketApiService (Retrofit) → OkHttpClient → MockTicketInterceptor → JSON response → GsonConverterFactory → DTO → toDomain() → Ticket`

---

## State Management

All screens use a `UiState<T>` sealed class:

```kotlin
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

Each ViewModel exposes a `StateFlow<UiState<T>>` collected by the screen via `collectAsState()`. The screen renders the appropriate composable for each state without any imperative logic.

---

## Architectural Decisions

| Decision | Rationale |
|----------|-----------|
| MVVM over MVI | Sufficient for a PoC; MVI adds boilerplate without benefit at this scale |
| SharedFlow EventBus over shared ViewModel | Keeps ViewModels independent; avoids tight coupling between screens |
| Object FeatureFlags over DI-injected service | Zero overhead, appropriate for PoC; easy to replace later |
| MockInterceptor vs. hardcoded mock data | Interceptor keeps the full networking stack active, making the swap to real API trivial |
| Single TicketRepository | Follows single responsibility; if data sources multiply, split per use-case |

---

## How to Evolve the Solution

1. **Real backend:** Remove `MockTicketInterceptor`, update `BASE_URL`, add an auth interceptor to attach the JWT token to every request.
2. **Remote feature flags:** Replace `FeatureFlags` field values with reads from Firebase Remote Config or a custom endpoint.
3. **Dependency injection:** Replace manual instantiation (`TicketRepository()` in ViewModels) with Hilt `@HiltViewModel` — one annotation change per ViewModel.
4. **Pagination:** `TicketApiService.getTickets()` can add `@Query("page")` and `@Query("size")`; the list screen adopts Paging 3 with minimal changes.
5. **Offline support:** Add a Room database between the repository and the network layer — the ViewModel observes the DB, the repository writes to DB after every successful fetch.
