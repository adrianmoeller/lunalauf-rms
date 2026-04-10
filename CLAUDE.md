# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What This Project Is

Luna-Lauf RMS (Round Management System) is a distributed desktop application for managing a charity running event. It tracks RFID-based round counting, runner/team scores, Telegram bot integration, and real-time scoreboard display across two cooperating apps.

## Build & Run Commands

```bash
# Build
./gradlew build                        # All modules
./gradlew :centerApp:build
./gradlew :counterApp:build

# Run
./gradlew :centerApp:run
./gradlew :counterApp:run

# Test (only utilities module has tests)
./gradlew :utilities:test
./gradlew :utilities:test --tests "ClassName"
./gradlew :utilities:test --tests "ClassName.methodName"

# Package (Windows MSI)
./gradlew :centerApp:packageMsi
./gradlew :counterApp:packageMsi
```

## Module Architecture

The project is split into six Gradle modules:

```
centerApp    ─┐
counterApp   ─┤── UI layers (Compose Desktop + Voyager navigation)
              │
modelAPI     ─┤── Model lifecycle: new/load/save/close files
model        ─┤── Domain model: Event (root aggregate), Runner, Team, Minigame, Challenge
              │
utilities    ─┘── Infrastructure: logging, TCP networking, Telegram bots, persistence
buildSrc         ── Convention plugins (kotlin-application, kotlin-library, kotlin-model)
```

**centerApp** — Main management UI. Organizes runners, teams, minigames, challenges, and connections. Opens a secondary window for the public scoreboard. Uses Voyager for tab/screen navigation and Material3.

**counterApp** — Lightweight scanner UI for RFID-connected devices. Connects to centerApp via TCP and reports scanned chip IDs. Displays green/red feedback per scan.

**model** — Core domain. `Event` is the root aggregate owning all runners and teams. All model objects expose `MutableStateFlow` (as public `StateFlow`) for reactive UI binding. Mutations use `Mutex` for thread safety.

**modelAPI** — `ModelManager` manages file lifecycle (`ModelState`: Unloaded → Loading → Loaded). `PersistenceManager` is the persistence interface. All operations return sealed-class result types (e.g., `CreateRunnerResult`, `AddRoundResult`).

**utilities** — Shared infrastructure:
- `network/`: TCP socket client/server; `RoundCounter` and `InfoDisplay` connection types; Telegram bot integration
- `persistence/`: GSON + EMF/XMI serialization with a mapper pattern (internal model ↔ serialized model). File extension: `.llj`
- `logging/`: Custom SLF4J bridge that emits log records as a Kotlin `Flow` for real-time display in the UI

## Key Patterns

- **MVVM via Voyager**: UI screens have corresponding `ScreenModel` classes containing business logic and state
- **Reactive state**: `MutableStateFlow` exposed as `asStateFlow()`, collected in Compose composables
- **Result types**: Sealed classes used throughout modelAPI for typed error handling (never exceptions for expected cases)
- **Mapper pattern**: Separate mapper classes translate between the live domain model and the serializable representation
- **EMF integration**: `buildSrc`'s `kotlin-model` plugin configures Eclipse Modeling Framework for XMI-capable persistence

## Tech Stack

| Concern | Library |
|---|---|
| UI | Jetbrains Compose Desktop 1.7.3, Material3 |
| Navigation | Voyager 1.0.0 |
| Language/Runtime | Kotlin 2.0.21, Java 21 |
| Async | Kotlinx Coroutines 1.8.1 |
| Serialization | GSON 2.11.0, EMF 2.33.0 |
| Telegram | TelegramBots API 6.9.0 |
| Logging | SLF4J 2.0.13 + Log4j (Reload4j) |
| Icons | Font Awesome 1.1.0, Eva Icons 1.1.0 |
| Testing | JUnit Jupiter 5.8.2 |

## Release Process

GitHub Actions workflows:
- **build.yml**: Builds both apps on push/PR to `main`
- **release.yml**: Triggered by version tags (`v*.*.*`); creates MSI installers and a GitHub release
