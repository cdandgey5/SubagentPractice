---
name: android-scaffold
description: >
  Create an empty but fully buildable Android project shell (Gradle, manifest,
  theme, MainActivity, Hilt Application, version catalog) with no features. Use
  when the user wants a clean starting point to build on themselves, or as the
  first concrete step before adding features. Lighter than generate-android-app.
---

# Android Scaffold

Produces the minimal project that compiles and launches to an empty screen.
No data model, no feature screens — just the foundation.

## What it generates

```
<root>/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── gradlew / gradlew.bat
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/<pkg>/
        │   ├── MainApplication.kt   (@HiltAndroidApp)
        │   ├── MainActivity.kt      (@AndroidEntryPoint, Compose)
        │   └── ui/theme/            (Color/Type/Theme)
        └── res/                     (strings, themes, icons)
```

## How to run it
1. Ask for (or infer) the **app name** and **package name**.
2. Delegate the build files to the `android-build-engineer` subagent and the
   theme + MainActivity to the `android-ui-composer` subagent, both with a
   minimal spec (no entities, one empty Home screen).
3. Verify it assembles if an SDK is available.

Use `templates/` in this repo as the canonical reference for file contents.
