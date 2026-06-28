# Why this design? (with diagrams)

This document explains **why** the Android generator is built as many subagents +
skills + an orchestrator, rather than one big prompt. Each section pairs the
reasoning with a diagram.

---

## 1. The core problem: one prompt can't do it well

A naive approach is a single giant prompt: *"generate a full Android app."* That
fails for predictable reasons.

```mermaid
flowchart TD
    P[One mega-prompt:<br/>'generate the whole app'] --> X1[Context bloat:<br/>build + data + UI + tests<br/>all in one window]
    P --> X2[Conflicting concerns:<br/>gradle versions vs. compose code<br/>compete for attention]
    P --> X3[No fresh reviewer:<br/>author grades own work]
    P --> X4[Hard to reuse:<br/>can't just 'add one screen']
    X1 & X2 & X3 & X4 --> F([Inconsistent, often<br/>non-building output])
```

Splitting the job fixes each failure mode:

```mermaid
flowchart TD
    P[Decomposed system] --> G1[Each agent has a<br/>small, focused context]
    P --> G2[One concern per agent:<br/>no competition]
    P --> G3[Independent reviewer<br/>at the end]
    P --> G4[Skills expose<br/>reusable entry points]
    G1 & G2 & G3 & G4 --> S([Consistent, buildable output])
```

---

## 2. Separation of concerns → one agent per layer

An Android app has naturally separable layers. We map **one specialist subagent
to each layer**, mirroring how a real engineering team is structured.

```mermaid
flowchart LR
    subgraph App layers
        direction TB
        UI[UI layer<br/>Compose + ViewModels]
        DOM[Domain layer<br/>models + interfaces]
        DATA[Data layer<br/>Room + repositories]
        BUILD[Build + manifest]
        TEST[Tests]
    end
    UI -.owned by.-> A1[android-ui-composer]
    DOM -.owned by.-> A2[android-architect]
    DATA -.owned by.-> A3[android-data-engineer]
    BUILD -.owned by.-> A4[android-build-engineer]
    TEST -.owned by.-> A5[android-test-engineer]
```

**Why this matters:** each agent's system prompt can be dense with the idioms and
failure modes of *just* its layer. The build engineer obsesses over
AGP↔Kotlin↔Compose version compatibility; the UI composer obsesses over
unidirectional data flow. Neither dilutes the other.

---

## 3. The orchestrator: a coordinator, not a coder

The orchestrator never writes feature code. Its only job is **decompose →
delegate → integrate → review**. This keeps coordination logic out of the
specialists and keeps specialists out of planning.

```mermaid
sequenceDiagram
    participant U as User
    participant S as /generate-android-app
    participant O as Orchestrator
    participant A as Architect
    participant W as Specialists (build/data/ui/test)
    participant R as Reviewer

    U->>S: prompt: "habit tracker"
    S->>O: delegate idea
    O->>O: plan: features, screens, data model, PROJECT_SPEC
    O->>A: PROJECT_SPEC
    A-->>O: package tree + contracts (interfaces)
    O->>W: PROJECT_SPEC + contracts (parallel)
    W-->>O: build files, data layer, UI, tests
    O->>O: integrate / wire Hilt / nav
    O->>R: full project
    R-->>O: blocking issues + fixes
    O-->>U: report + build command
```

---

## 4. Contracts are the secret to parallelism

The architect produces **interfaces** (domain repositories) *before* any
implementation. Because the data engineer and UI composer both code against the
same interface, they can work **in parallel** and still fit together.

```mermaid
flowchart TB
    A[android-architect] -->|defines| I[FeatureRepository interface<br/>pure Kotlin, no Android]
    I --> D[android-data-engineer<br/>writes FeatureRepositoryImpl<br/>backed by Room]
    I --> U[android-ui-composer<br/>writes FeatureViewModel<br/>injects FeatureRepository]
    D -.Hilt binds impl to interface.-> X((Wire-up))
    U -.consumes interface.-> X
    X --> APP([Layers fit without<br/>knowing each other])
```

This is the dependency-inversion principle applied to *agent coordination*: the
seam (interface) lets two agents who never talk to each other produce compatible
code.

```mermaid
flowchart LR
    UI[UI layer] -->|depends on| DOM[Domain interfaces]
    DATA[Data layer] -->|implements| DOM
    DOM -.->|knows nothing about| UI
    DOM -.->|knows nothing about| DATA
```

---

## 5. Why skills *and* subagents (they're different tools)

Skills and subagents solve different problems. We use both deliberately.

```mermaid
flowchart TB
    subgraph Skills [Skills = the command surface]
        direction TB
        SK1[/generate-android-app/]
        SK2[/android-scaffold/]
        SK3[/compose-screen/]
        SK4[/gradle-setup/]
        SK5[/android-conventions/]
    end
    subgraph Agents [Subagents = the workers]
        direction TB
        AG1{{orchestrator}}
        AG2[architect]
        AG3[build]
        AG4[data]
        AG5[ui]
        AG6[test]
        AG7[reviewer]
    end
    SK1 --> AG1
    AG1 --> AG2 & AG3 & AG4 & AG5 & AG6 & AG7
    SK2 --> AG3 & AG5
    SK3 --> AG5 & AG4 & AG6
    SK4 --> AG3
```

| | Skill | Subagent |
|---|-------|----------|
| **What it is** | A discoverable, named command (`/foo`) | A worker with its own context + tools |
| **Runs in** | The main conversation | An isolated context window |
| **Best for** | A stable entry point / workflow | Heavy, focused work that shouldn't pollute main context |
| **Here** | How a user *starts* a task | How the task *gets done* |

The skill is the **doorknob**; the subagents are the **room behind it**. A user
shouldn't have to know which seven agents run — they type one command.

---

## 6. Why a separate reviewer agent

The agent that writes code is the worst judge of it — it's anchored on its own
choices and its context is full of justifications. A **fresh reviewer** with an
empty context and an adversarial checklist catches what authors miss.

```mermaid
flowchart LR
    AUTH[Specialists<br/>context full of<br/>their own decisions] --> CODE[Generated code]
    CODE --> REV[android-code-reviewer<br/>fresh context<br/>adversarial checklist]
    REV -->|Blocking| FIX[Must fix before delivery]
    REV -->|Should-fix| NOTE[Noted to user]
    REV -->|Nits| LOG[Logged]
```

---

## 7. Model selection per agent

Coordination and judgment get the strongest model; mechanical generation uses a
faster one. This is a cost/latency vs. capability trade-off made per role.

```mermaid
flowchart TB
    subgraph opus [opus — judgment / planning]
        O1[orchestrator]
        O2[architect]
        O3[reviewer]
    end
    subgraph sonnet [sonnet — focused generation]
        S1[build-engineer]
        S2[data-engineer]
        S3[ui-composer]
        S4[test-engineer]
    end
```

---

## 8. Why templates exist

LLMs drift on exact Gradle versions and plugin syntax — the #1 cause of a
generated Android app failing to build. The `templates/` directory pins a
**known-good** baseline so agents adapt rather than invent.

```mermaid
flowchart LR
    T[templates/<br/>known-good Gradle + Kotlin] --> BE[build-engineer<br/>adapts versions]
    T --> UI[ui-composer<br/>copies MVVM shape]
    T --> DE[data-engineer<br/>copies repo pattern]
    BE & UI & DE --> OUT([Lower chance of<br/>build failure])
```

---

## 9. How it all composes

```mermaid
flowchart TD
    U([User]) -->|/generate-android-app idea| SK[Skill]
    SK --> O{{Orchestrator}}
    O -->|1 plan| SPEC[PROJECT_SPEC<br/>docs/PROJECT_SPEC.md]
    SPEC --> ARC[Architect]
    ARC -->|contracts| CT[Domain interfaces]
    CT --> P{Parallel fan-out}
    P --> BE[Build]
    P --> DE[Data]
    P --> UIc[UI]
    P --> TE[Tests]
    BE & DE & UIc & TE --> INT[Orchestrator integrates]
    INT --> REV[Reviewer]
    REV --> DONE([Buildable app + report])
    CONV[[android-conventions<br/>shared rulebook]] -.guides.-> ARC & BE & DE & UIc & TE & REV
```

The `android-conventions` skill is the connective tissue — every agent conforms
to it, which is *why* independently generated layers end up consistent.

---

## 10. Why a dedicated network engineer

Networking is its own discipline: serialization, timeouts, retries, error
mapping, and the offline/online dance. Folding it into the data engineer would
bloat that agent with two unrelated concerns (local DB *and* HTTP). So it gets
its own specialist — and, crucially, it introduces a **third model type**.

```mermaid
flowchart LR
    JSON[(Remote JSON)] --> DTO[DTO<br/>@Serializable<br/>data.remote.dto]
    DTO -->|mapper| MODEL[Domain model<br/>pure Kotlin]
    MODEL -->|mapper| ENT[Entity<br/>@Entity<br/>data.local]
    ENT --> DB[(Room)]
    MODEL --> VM[ViewModel]
    style MODEL fill:#1f6f3f,color:#fff
```

The rule the agent enforces: **DTO ≠ Entity ≠ domain model**, mapped at each
boundary. A DTO must never reach a ViewModel. This keeps a change in the API's
JSON shape from rippling into the UI.

### Offline-first: Room stays the single source of truth

When the app both fetches and persists, the UI never observes the network
directly — it observes Room. The network only *fills* Room.

```mermaid
sequenceDiagram
    participant V as Screen
    participant VM as ViewModel
    participant R as Repository
    participant DB as Room (source of truth)
    participant NET as RemoteDataSource (Retrofit)

    V->>VM: open / pull-to-refresh
    VM->>R: observeAll()  (cold start)
    R-->>VM: Flow from Room (cached)
    VM->>R: refresh()
    R->>NET: fetchFeatures()
    NET-->>R: NetworkResult.Success(list) / Error
    R->>DB: upsertAll(list)
    DB-->>R: Flow re-emits
    R-->>VM: fresh data (no extra call)
    Note over VM,V: One observable path; offline shows last cache
```

This is why networking still codes against the **same domain repository
interface** — the UI doesn't know or care whether data came from disk or the
wire. The network engineer slots in behind the existing contract.

```mermaid
flowchart TB
    I[FeatureRepository interface] --> IMPL[FeatureRepositoryImpl]
    IMPL --> DAO[Room DAO]
    IMPL --> RDS[RemoteDataSource<br/>Retrofit]
    style I fill:#1f6f3f,color:#fff
```

## 11. Why CI (Continuous Integration)

> Note: you asked about "android cli" — I read that as **CI** (the GitHub Actions
> idea I offered). Here's why it earns its place in a *code generator*.

The single biggest failure mode of any code generator is output that **looks
correct but doesn't compile**. An LLM can produce plausible Gradle and Kotlin
that has a subtly wrong version pin or a missing plugin. You only find out when
you build it.

CI closes that gap. It builds the generated app in a **clean cloud machine** on
every push and pull request — no dependence on whether you have an Android SDK
installed locally.

```mermaid
flowchart LR
    GEN[Generator produces app] --> PUSH[git push / open PR]
    PUSH --> CI{GitHub Actions}
    CI --> A[assembleDebug]
    CI --> B[testDebugUnitTest]
    CI --> C[lintDebug]
    A & B & C --> V{All pass?}
    V -->|yes| GREEN([✅ recorded proof it builds])
    V -->|no| RED([❌ artifacts uploaded<br/>fix before merge])
```

What this buys a generation pipeline specifically:

```mermaid
flowchart TD
    CI[CI on every change] --> R1[Catches version-pin drift<br/>the reviewer might miss]
    CI --> R2[Verifies without a local SDK<br/>'it builds' becomes a fact]
    CI --> R3[Protects against regressions<br/>when you add screens later]
    CI --> R4[Runs unit tests + lint<br/>not just compilation]
    CI --> R5[Reproducible: same JDK,<br/>pinned actions, cached Gradle]
```

It pairs naturally with the **reviewer**: the reviewer reasons about the code;
CI *executes* it. Reasoning can be wrong; a green build cannot.

```mermaid
flowchart LR
    CODE[Generated code] --> REV[Reviewer<br/>static reasoning]
    CODE --> CI[CI<br/>actual compilation]
    REV --> CONF{Both agree?}
    CI --> CONF
    CONF -->|yes| TRUST([High confidence ship])
    CONF -->|reviewer ok, CI red| BUG([Found a real build bug])
```

This is why CI is exposed both as an orchestrator stage *and* a standalone
`/setup-ci` skill — it's useful on any project, not just freshly generated ones.

## Summary of the trade-offs

| Decision | Benefit | Cost we accept |
|----------|---------|----------------|
| Many subagents vs. one prompt | Focus, smaller contexts, parallelism | More orchestration plumbing |
| Contracts before code | Independent, compatible layers | An extra up-front stage |
| Separate reviewer | Catches author blind spots | One more pass |
| Skills as entry points | Simple UX, reusable workflows | More files to maintain |
| Pinned templates | Reliable builds | Must keep versions current |
| Per-agent model choice | Cost/speed where it's fine, power where it counts | Slightly more config |
| Dedicated network engineer | Clean DTO/Entity/domain separation; offline-first | A third model type to map |
| CI on every push | Executes the code; proves it builds without a local SDK | CI minutes; a workflow to maintain |
