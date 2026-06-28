---
name: setup-ci
description: >
  Add GitHub Actions CI to a generated Android project so it builds and tests on
  every push and pull request. Use when the user wants CI, asks to "make it build
  on GitHub", or wants automated proof the generated app compiles. Invokes the
  android-ci-engineer subagent.
---

# Setup CI

Adds a GitHub Actions workflow that compiles the app and runs its checks in a
clean cloud environment — the automated guarantee that a generated Android app
actually builds, independent of anyone's local SDK.

## How to run it
1. Confirm the project has a committed Gradle wrapper (`gradlew`,
   `gradle/wrapper/`). If missing, ask `android-build-engineer` to add it first —
   CI cannot run without it.
2. Delegate to the `android-ci-engineer` subagent.
3. It writes `.github/workflows/android-ci.yml` from `templates/ci/android-ci.yml`,
   adapting branch names and job matrix to the project.
4. Report what runs on PRs (build + unit tests + lint) vs. what's deferred
   (instrumented/emulator jobs).

## What the workflow does
- On `push` and `pull_request`: checkout → JDK 17 → cached Gradle →
  `assembleDebug` → `testDebugUnitTest` → `lintDebug`.
- Uploads test/lint reports as artifacts when a step fails.

## Why it's a skill
CI is a self-contained, reusable workflow you may want to run on any project —
not just freshly generated ones. Exposing it as `/setup-ci` makes it a one-step
command instead of a buried stage of the generator.
