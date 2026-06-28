# PROJECT_SPEC — Daily Quotes

The single source of truth for this generation run. Every subagent receives this.

## Idea
A "Daily Quotes" app: browse inspirational quotes fetched from a remote API,
and favorite the ones you like. Favorites are cached locally and available
offline.

## Stack (defaults)
| Choice        | Value                                   |
|---------------|-----------------------------------------|
| Language      | Kotlin                                  |
| UI            | Jetpack Compose + Material 3            |
| Architecture  | MVVM + Repository + thin domain layer   |
| DI            | Hilt                                    |
| Persistence   | Room (KSP), offline-first               |
| Networking    | Retrofit + OkHttp + kotlinx.serialization |
| minSdk        | 24                                      |
| compileSdk/targetSdk | 35                               |
| package       | `com.example.dailyquotes`               |
| app name      | Daily Quotes                            |

## Features
1. View a scrollable list of quotes fetched from the network.
2. Pull-to-refresh / refresh button to fetch the latest quotes.
3. Toggle a quote as favorite (heart icon).
4. Favorites + last-fetched quotes are cached in Room and shown offline.
5. Filter toggle: "All" vs "Favorites".

## Screens / navigation
- `QuotesScreen` (start destination): the list, a refresh action, a
  All/Favorites filter, and a heart toggle per row.
- (Single-screen app; no nav graph beyond the start destination, but wire a
  `NavHost` with one route so adding screens later is trivial.)

## Data model
- Domain model `Quote`: `id: String`, `content: String`, `author: String`,
  `isFavorite: Boolean`.

## Remote API
- Base URL: `https://api.quotable.io/`
- `GET quotes/random?limit=20` → array of quote objects
  (`_id`, `content`, `author`). The app maps these to `Quote` with
  `isFavorite = false` unless already favorited locally.
- (If the API is unreachable in CI, the offline cache still drives the UI; the
  network path is exercised by unit tests with a fake remote source.)

## Repository contract (architect owns the exact signatures)
- `QuoteRepository`:
  - `fun observeQuotes(favoritesOnly: Boolean): Flow<List<Quote>>`
  - `suspend fun refresh(): NetworkResult<Unit>`
  - `suspend fun setFavorite(id: String, favorite: Boolean)`

## Layer ownership for this run
- **architect** → domain models + `QuoteRepository` interface + package dirs.
- **build-engineer** → Gradle (Kotlin DSL + version catalog), manifest, res, wrapper.
- **data-engineer** → Room entity/DAO/db, entity↔domain mappers, `DataModule`.
- **network-engineer** → Retrofit API, DTOs, `NetworkModule`, `RemoteDataSource`,
  and the offline-first `QuoteRepositoryImpl` (combines DAO + remote).
- **ui-composer** → theme, `NavHost`, `QuotesScreen`, `QuotesViewModel`,
  `MainActivity`, `MainApplication`.
- **test-engineer** → ViewModel unit test (fake repo), DAO instrumented test.
- **ci-engineer** → GitHub Actions workflow.
