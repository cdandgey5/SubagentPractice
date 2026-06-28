# Architecture — how the pipeline executes

This document describes the runtime mechanics of the generator: the stages, what
flows between them, and the structure of the app it produces.

For the *reasoning* behind these choices, see [`WHY.md`](./WHY.md).

---

## 1. The five stages

```mermaid
flowchart LR
    S1[1 Plan] --> S2[2 Architect]
    S2 --> S3[3 Generate<br/>parallel]
    S3 --> S4[4 Integrate]
    S4 --> S5[5 Review]
    S5 --> OUT([Buildable app])
```

| Stage | Owner | Input | Output |
|-------|-------|-------|--------|
| 1. Plan | orchestrator | user prompt | `PROJECT_SPEC` |
| 2. Architect | architect | `PROJECT_SPEC` | package tree + contracts |
| 3. Generate | build / data / ui / test | spec + contracts | layer code |
| 4. Integrate | orchestrator | all layer code | wired project |
| 5. Review | reviewer | wired project | fixes + report |

---

## 2. The PROJECT_SPEC contract

Everything downstream depends on a single shared artifact the orchestrator writes
in stage 1. It is saved to `docs/PROJECT_SPEC.md` for the run.

```mermaid
classDiagram
    class PROJECT_SPEC {
        +String appName
        +String packageName
        +int minSdk
        +int targetSdk
        +String[] features
        +Screen[] screens
        +Entity[] dataModel
        +NavGraph navigation
        +Choices stack (Kotlin/Compose/Hilt/Room)
    }
    class Screen {
        +String name
        +String purpose
        +String[] uiStateFields
    }
    class Entity {
        +String name
        +Field[] fields
        +String[] relations
    }
    PROJECT_SPEC --> Screen
    PROJECT_SPEC --> Entity
```

Because all agents receive the *same* spec, their outputs line up without the
agents communicating directly.

---

## 3. Delegation flow (who calls whom)

```mermaid
flowchart TD
    SK[/generate-android-app skill/] --> O{{orchestrator}}
    O -->|Agent tool| A[architect]
    O -->|Agent tool, parallel| B[build-engineer]
    O -->|Agent tool, parallel| D[data-engineer]
    O -->|Agent tool, parallel| C[ui-composer]
    O -->|Agent tool, parallel| T[test-engineer]
    O -->|Agent tool| R[reviewer]
    A -.returns contracts.-> O
    B & D & C & T -.return code.-> O
    R -.returns report.-> O
```

Only the orchestrator uses the `Agent` tool. Specialists have file + search tools
but **not** `Agent` — they cannot spawn further agents, which keeps the call
graph shallow and predictable.

---

## 4. Data flow between stages

```mermaid
flowchart LR
    PR([prompt]) --> SPEC[PROJECT_SPEC]
    SPEC --> CON[contracts<br/>domain interfaces]
    SPEC --> CON
    CON --> GRADLE[Gradle + manifest]
    CON --> DATAC[Room + repo impls]
    CON --> UIC[Compose + viewmodels]
    CON --> TESTS[tests vs. contracts]
    GRADLE & DATAC & UIC & TESTS --> PROJ[wired project]
    PROJ --> RPT[review report]
```

---

## 5. The generated app's architecture (MVVM + Clean-ish)

This is the shape every generated project takes.

```mermaid
flowchart TB
    subgraph UI [ui layer - Android]
        SC[Screen Composable] -->|collects StateFlow| VM[ViewModel]
        VM -->|events via lambdas| SC
    end
    subgraph DOM [domain layer - pure Kotlin]
        REPO[Repository interface]
        MODEL[Domain model]
    end
    subgraph DATA [data layer - Android/Room]
        IMPL[RepositoryImpl] --> DAO[DAO]
        DAO --> DB[(Room database)]
        IMPL -->|maps Entity to Model| ENT[Entity]
    end
    VM -->|injected| REPO
    IMPL -->|implements| REPO
    VM --> MODEL
    IMPL --> MODEL
```

**Dependency rule:** arrows of dependency point inward to `domain`. The UI and
data layers depend on domain interfaces; domain depends on nothing Android.

---

## 6. Unidirectional data flow inside a screen

```mermaid
sequenceDiagram
    participant V as View (Composable)
    participant VM as ViewModel
    participant R as Repository (domain)
    participant DB as Room

    V->>VM: user event (onClick)
    VM->>R: suspend upsert(model)
    R->>DB: DAO write
    DB-->>R: Flow emits new list
    R-->>VM: Flow<List<Model>>
    VM->>VM: update StateFlow<UiState>
    VM-->>V: new UiState (recomposition)
```

---

## 7. Generated package layout

```mermaid
flowchart TD
    ROOT[com.example.app] --> DI[di]
    ROOT --> DATA[data]
    ROOT --> DOMAIN[domain]
    ROOT --> UI[ui]
    ROOT --> APP[MainApplication + MainActivity]
    DATA --> LOCAL[data.local<br/>entities, DAOs, db]
    DATA --> REPOIMPL[data.repository<br/>impls]
    DOMAIN --> DMODEL[domain.model]
    DOMAIN --> DREPO[domain.repository<br/>interfaces]
    UI --> THEME[ui.theme]
    UI --> NAV[ui.navigation]
    UI --> FEAT[ui.feature-*]
    UI --> COMP[ui.components]
```

---

## 8. Build dependency graph (Gradle)

```mermaid
flowchart LR
    SETTINGS[settings.gradle.kts] --> APPMOD[:app]
    CATALOG[libs.versions.toml] --> APPMOD
    ROOTB[root build.gradle.kts<br/>plugins apply false] --> APPMOD
    APPMOD --> COMPOSE[Compose BOM]
    APPMOD --> HILT[Hilt + KSP]
    APPMOD --> ROOM[Room + KSP]
    APPMOD --> NAV[Navigation Compose]
    APPMOD --> LIFECYCLE[Lifecycle]
```

---

## 9. Failure handling

```mermaid
flowchart TD
    GEN[Specialist returns code] --> CHK{Integrate:<br/>contracts match?}
    CHK -->|yes| REV[Reviewer]
    CHK -->|no| FIXO[Orchestrator edits<br/>mismatch] --> CHK
    REV --> RES{Blocking issues?}
    RES -->|none| DONE([Deliver])
    RES -->|structural| BACK[Hand back to<br/>orchestrator] --> CHK
    RES -->|small| SELF[Reviewer self-fixes] --> DONE
```

---

## 10. Extending the system

To add a capability, add a **skill** (entry point) and/or a **subagent** (worker):

```mermaid
flowchart LR
    NEW[New need:<br/>e.g. 'add networking layer'] --> Q{Reusable<br/>command?}
    Q -->|yes| ADDSK[Add skill in<br/>.claude/skills/]
    Q -->|heavy focused work| ADDAG[Add subagent in<br/>.claude/agents/]
    ADDSK --> WIRE[Orchestrator learns<br/>to delegate to it]
    ADDAG --> WIRE
```

Examples of natural extensions: an `android-network-engineer` (Retrofit/Ktor),
an `android-ci-engineer` (GitHub Actions), or a `play-store-prep` skill.
