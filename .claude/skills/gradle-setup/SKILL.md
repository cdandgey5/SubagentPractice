---
name: gradle-setup
description: >
  Create or repair the Gradle build for an Android project: version catalog,
  build.gradle.kts files, plugins, dependencies, manifest, and wrapper. Use when
  builds fail due to configuration, versions are mismatched, or a project needs
  its build files (re)generated without touching feature code.
---

# Gradle Setup

Focused on **build configuration only** — the thing most likely to break a
generated Android project, and the thing most worth isolating into one expert.

## How to run it
1. Inspect the current state: `Glob` for `*.gradle.kts`, `libs.versions.toml`,
   `AndroidManifest.xml`; read any build error the user pasted.
2. Delegate to the `android-build-engineer` subagent with the symptoms.
3. Focus areas, in order of how often they break:
   - **Version compatibility:** AGP ↔ Kotlin ↔ Compose compiler ↔ Compose BOM.
   - **Plugin application:** Hilt + KSP applied in `app/build.gradle.kts`.
   - **Catalog references:** every `libs.*` alias resolves.
   - **Manifest:** Application class + Activity declared, permissions present.
4. Verify with `./gradlew assembleDebug` if an SDK exists; otherwise sanity-check
   statically and say so.

## Reference
`templates/` holds known-good build files. Treat them as the baseline and adapt
versions to the latest mutually compatible set.
