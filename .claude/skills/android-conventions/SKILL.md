---
name: android-conventions
description: >
  The shared rulebook every Android subagent follows: architecture, naming,
  MVVM/Compose patterns, threading, and persistence conventions. Use as a
  reference when generating or reviewing Android code so all subagents produce
  consistent output. Read this when unsure how the pipeline expects code to look.
---

# Android Conventions

The single source of style/architecture truth for the pipeline. Every subagent
should conform to this so independently generated layers fit together.

## Architecture
- **MVVM + Repository**, with a thin **domain** layer of models + repository
  interfaces. UI → domain ← data. Domain is pure Kotlin (no Android, no Room).
- Unidirectional data flow: state down via `StateFlow<UiState>`, events up via
  lambdas.

## Packages
```
<pkg>.di | data.local | data.repository | data.model
        | domain.model | domain.repository
        | ui.theme | ui.navigation | ui.<feature> | ui.components
```

## Naming
- Screens: `FooScreen.kt` (composable `FooScreen`). ViewModels: `FooViewModel`.
- UiState: nested `data class FooUiState` or a sealed `FooUiState`.
- Entities: `FooEntity`; domain model: `Foo`; DAO: `FooDao`; repo interface:
  `FooRepository`; impl: `FooRepositoryImpl`.

## Compose / UI
- Material 3 only; theme tokens, no hardcoded colors.
- Collect state with `collectAsStateWithLifecycle()`.
- Stateless, hoisted composables; `@Preview` per screen.
- Handle loading / empty / error / content explicitly.

## Coroutines / threading
- ViewModel work in `viewModelScope`; repository suspend funs main-safe.
- Observable reads return `Flow`; collect on the main thread, do work off it.

## Persistence
- Room via KSP. Entities never leave the repository; map to domain models.
- `Flow<List<T>>` for reactive reads; `suspend fun` for writes.

## DI
- Hilt. `@HiltAndroidApp` app, `@AndroidEntryPoint` activity,
  `@HiltViewModel` view models. Bind repository impls to interfaces in modules.

## Testing
- Unit-test ViewModels against **fake repositories** implementing the domain
  interface. Instrumented-test DAOs with an in-memory database.

## Build
- Gradle Kotlin DSL + version catalog (`libs.versions.toml`). Compose BOM.
  Pin a known-good AGP/Kotlin/Compose set.
