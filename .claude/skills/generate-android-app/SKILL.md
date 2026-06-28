---
name: generate-android-app
description: >
  Generate a complete, buildable Android app from a single natural-language
  prompt (e.g. "/generate-android-app a habit tracker with daily reminders").
  Use this when the user wants a whole new Android project scaffolded end to
  end. It invokes the android-orchestrator subagent, which plans the app and
  delegates to the specialist subagents.
---

# Generate Android App

This skill is the **entry point** to the Android generation pipeline. It takes a
free-form app idea and produces a complete Kotlin + Jetpack Compose project.

## How to run it

When invoked, the argument is the app idea. Steps:

1. **Capture the idea.** Treat everything after the skill name as the prompt.
   If it's empty, ask the user for a one-line description.
2. **Delegate to the orchestrator.** Spawn the `android-orchestrator` subagent
   with the idea. Let it own planning and delegation — do **not** write feature
   code from this skill directly.
3. **Surface the plan.** Relay the orchestrator's `PROJECT_SPEC` to the user
   before heavy generation if the idea is large or ambiguous; otherwise proceed.
4. **Report back.** When the orchestrator finishes, summarize: app name, feature
   list, package/module tree, and the build command.

## Defaults (override only if the prompt says so)

| Choice        | Default                          |
|---------------|----------------------------------|
| Language      | Kotlin                           |
| UI            | Jetpack Compose + Material 3     |
| Architecture  | MVVM + Repository (+ light domain layer) |
| DI            | Hilt                             |
| Persistence   | Room (KSP)                       |
| minSdk        | 24                               |
| targetSdk     | latest stable                    |
| Build         | Gradle Kotlin DSL + version catalog |

## Why a skill *and* subagents?

The skill is the stable, discoverable command surface (`/generate-android-app`).
The subagents are the workers. Keeping them separate means the command can stay
simple while the heavy, context-hungry generation runs in isolated subagent
contexts that don't pollute the main conversation. See `docs/WHY.md`.

## Related skills
- `android-scaffold` — just the empty buildable shell, no features.
- `compose-screen` — add one screen to an existing project.
- `android-conventions` — the rules every subagent follows.
- `gradle-setup` — fix or create the build configuration only.
