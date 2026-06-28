---
name: compose-screen
description: >
  Add a single Jetpack Compose screen (Screen composable + Hilt ViewModel with
  StateFlow UiState + a nav route) to an existing generated Android project. Use
  when the user wants to add one feature/screen rather than a whole new app.
---

# Compose Screen

Adds one feature screen to an existing project, following the same MVVM pattern
the pipeline uses, so it's indistinguishable from generated code.

## Inputs
- Screen name (e.g. `Settings`, `HabitDetail`).
- What it shows / does (drives the `UiState` fields and ViewModel methods).
- Which domain repository it reads/writes (if any).

## How to run it
1. Locate the existing package root and nav graph (`Glob` for `*NavHost*`,
   `Screen` sealed class).
2. Delegate to `android-ui-composer` with a focused spec for just this screen.
3. Wire the route into the existing `NavHost` and the sealed `Screen` list.
4. If the screen needs new persistence, first delegate the entity/DAO/repository
   work to `android-data-engineer`, then build the UI against the new interface.
5. Ask `android-test-engineer` for a ViewModel test with a fake repository.

## Output
The new `*Screen.kt`, `*ViewModel.kt`, an updated nav route, an `@Preview`, and
a unit test — all consistent with the existing conventions.
