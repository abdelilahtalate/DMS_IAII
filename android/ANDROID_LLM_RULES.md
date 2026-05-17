 be# Android LLM Coding Rules

Use this file as the mandatory instruction set for any LLM or developer editing the `android` app. The goal is to keep the app scalable, flexible, easy to modify, and consistent with the existing feature-based MVVM structure.

## Project Identity

- App package: `com.example.dms_app`
- UI stack: Kotlin, Jetpack Compose, Material 3
- Architecture: feature-based MVVM with light Clean Architecture layers
- Navigation: `core/navigation/NavGraph.kt` for root flow, `core/navigation/MainScreen.kt` for main bottom-tab flow
- Shared UI: `core/ui/components` and `core/ui/theme`
- Network: Retrofit and OkHttp in `core/network`
- Local persistence: DataStore helpers such as `core/database/TokenManager.kt`

## Golden Rule

Never solve a feature by placing all logic inside a Compose screen. Screens render UI and send user actions to a ViewModel. ViewModels coordinate state and use cases. Use cases call repositories. Repositories talk to APIs, WebSockets, DataStore, camera, location, or other data sources.

Expected flow:

```text
Screen -> ViewModel -> UseCase -> Repository -> Core service/API/local source
```

Data flows back as immutable state:

```text
Repository result -> UseCase -> ViewModel -> StateFlow<UiState> -> Screen
```

## Folder Contract

Keep this structure:

```text
app/src/main/java/com/example/dms_app/
  core/
    config/
    database/
    location/
    navigation/
    network/
    ui/
      components/
      theme/
    utils/
  feature_name/
    data/
    domain/
    presentation/
```

Use the existing feature names when extending existing behavior:

- `feature_auth`: splash, onboarding, login, signup, auth token logic
- `feature_device`: QR scanning, device pairing
- `feature_drive`: camera preview and drive stream setup
- `feature_livedrive`: live monitoring, AI result stream, alerts during a drive
- `feature_home`: dashboard/home screen
- `feature_alerts`: alert-related UI or logic
- `feature_profile`: profile-related UI or logic

If a feature is tiny, a missing layer is acceptable only when there is truly no responsibility for that layer yet. As soon as API calls, business rules, or complex state appear, create the correct layer.

## Layer Rules

### Presentation

Files belong in:

```text
feature_x/presentation/
feature_x/presentation/sub_screen/
```

Allowed responsibilities:

- Compose screens and reusable UI specific to this feature
- ViewModels
- `UiState` data classes
- one-time UI events using `SharedFlow` or callbacks

Required rules:

- Screen files end with `Screen.kt`.
- ViewModel files end with `ViewModel.kt`.
- UI state files end with `UiState.kt` when the state is not small enough to keep beside the ViewModel.
- Screens should collect state from the ViewModel and render it.
- Screens should call ViewModel methods for actions such as `login()`, `updateEmail()`, `toggleMonitoring()`, `pairDevice()`.
- Do not call `ApiClient`, Retrofit services, OkHttp, DataStore, camera APIs, or location APIs directly from a screen.
- Prefer `collectAsStateWithLifecycle()` when lifecycle-aware collection is available. Existing files may use `collectAsState()`, but new code should prefer lifecycle-aware collection.
- Use `LaunchedEffect` for navigation or one-time reactions to state changes.

### ViewModel

Allowed responsibilities:

- Own and expose immutable UI state.
- Run coroutines in `viewModelScope`.
- Call use cases.
- Convert domain/data results into UI state.
- Cancel jobs when stopping a stream, timer, camera, or location process.

Required pattern:

```kotlin
private val _state = MutableStateFlow(FeatureUiState())
val state: StateFlow<FeatureUiState> = _state
```

Rules:

- Never expose `MutableStateFlow` publicly.
- Use `copy()` to update immutable state.
- Keep user input methods small: `updateEmail(value)`, `updatePassword(value)`, etc.
- Wrap network and stream work in `try/catch` or Flow `.catch {}`.
- Store loading, success, and error states in `UiState`.
- Use `MutableSharedFlow` only for one-time events such as alert sounds, snackbars, or navigation events.
- If a ViewModel starts a long-running job, keep a `Job?` reference and cancel it before starting again.
- Do not put large JSON parsing, networking setup, or UI drawing logic in the ViewModel.

### Domain

Files belong in:

```text
feature_x/domain/
```

Allowed responsibilities:

- Single-purpose use cases.
- Business decisions that belong to the feature and are not Android UI details.
- Calling one or more repository methods.

Required rules:

- Use case files end with `UseCase.kt`.
- Use cases should expose `operator fun invoke(...)`.
- Keep each use case focused on one action: `LoginUseCase`, `SignupUseCase`, `StartAnalysisUseCase`, `ObserveAiResultsUseCase`.
- Do not import Compose, Android views, Retrofit annotations, or UI components in domain files.
- If a rule is shared by multiple features, move it to `core/utils` or a clear shared domain helper.

Example:

```kotlin
class LoginUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): AuthResponse {
        return repository.login(request)
    }
}
```

### Data

Files belong in:

```text
feature_x/data/
```

Allowed responsibilities:

- Repositories.
- API calls through `ApiClient.apiService`.
- WebSocket streams.
- DataStore reads/writes when feature-specific.
- Camera, QR, location, and stream data access when feature-specific.

Required rules:

- Repository files end with `Repository.kt`.
- Repositories should hide data-source details from ViewModels.
- Repositories may return suspend results or `Flow`.
- WebSocket and callback APIs should be wrapped in `callbackFlow`.
- Use `awaitClose` to close sockets/listeners/resources.
- Do not import Compose UI from data files.
- Do not store UI strings or UI colors in repositories.

## Core Rules

Use `core` only for shared app infrastructure.

- `core/config`: app configuration such as host URLs.
- `core/network`: Retrofit interfaces, request/response DTOs, `ApiClient`.
- `core/navigation`: app route graph and bottom-tab graph.
- `core/ui/theme`: colors, typography, shape, theme preference.
- `core/ui/components`: reusable app-wide Compose components.
- `core/location`: location client/repository used by multiple features.
- `core/database`: app-wide local storage helpers.
- `core/utils`: small shared utilities.

Do not put feature-specific UI or business logic in `core`. If only one feature uses it, keep it inside that feature.

## Networking Rules

- Add new REST endpoints to `core/network/ApiService.kt`.
- Add request/response DTOs near the API service unless the DTO is large enough to deserve its own file.
- Repositories call `ApiClient.apiService`; screens and ViewModels do not.
- Keep backend URL configuration in `core/config/AppConfig.kt`.
- Do not hardcode IP addresses, ports, or endpoint roots inside screens.
- For streaming or infinite responses, avoid logging/buffering response bodies.
- Always handle connection loss and return an error state instead of crashing.

## Navigation Rules

- Root auth/setup routes belong in `AppNavGraph`.
- Main app tab routes belong in `MainScreen`.
- Do not pass large objects through routes. Pass IDs or encoded strings only.
- Encode URLs and secrets before placing them in a route.
- Prefer route constants or sealed route objects when adding many new routes.
- Navigation callbacks should be passed into screens; screens should not create root navigation controllers themselves.

## Compose UI Rules

- Use `DriveMindTheme` from `MainActivity`.
- Use `DriveMindBackground` for full screens unless a screen has a specific existing pattern.
- Use colors from `LocalAppColors.current`.
- Reuse shared components from `core/ui/components` before creating new component styles.
- Keep feature-specific components inside that feature until they are reused by multiple features.
- Avoid duplicating button, card, chip, input, or bottom-nav styles.
- Keep screens readable by extracting large sections into private composables in the same file or feature folder.
- Do not place networking, parsing, storage, or long-running loops inside composables.
- Avoid magic layout values repeated across screens; reuse existing spacing/style patterns.

## State And Error Rules

Every feature with user interaction should have a clear `UiState`.

Recommended fields:

```kotlin
data class FeatureUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
```

Rules:

- Store user-editable fields in state.
- Store loading status in state.
- Store recoverable errors in state.
- Clear stale errors before starting a new action.
- Never throw exceptions from a ViewModel to the UI.
- Keep state immutable.
- Use nullable fields only when absence has meaning.

## Scalability Rules

When adding a new feature:

1. Create `feature_newname`.
2. Add `presentation` with `NewNameScreen`, `NewNameViewModel`, and `NewNameUiState`.
3. Add `domain` use cases if there is any business action.
4. Add `data` repository if there is any API/local/stream source.
5. Add API DTOs and endpoints in `core/network` if needed.
6. Register routes in `core/navigation`.
7. Reuse `core/ui/components` and `core/ui/theme`.
8. Add focused tests for use cases or repositories when logic is non-trivial.

Do not modify unrelated features while adding a new one.

## Flexibility Rules

- Prefer constructor parameters for dependencies where possible.
- Current code uses manual dependency creation in ViewModels; follow it for consistency until dependency injection is introduced.
- If adding dependency injection later, migrate feature by feature and keep public behavior unchanged.
- Keep use cases small so they can be replaced, mocked, or tested.
- Keep repositories behind clear methods so API changes do not rewrite UI.
- Do not let backend JSON field names leak into UI naming when a better UI name exists. Map data in the ViewModel or use case.

## File Naming Rules

Use these names:

```text
FeatureScreen.kt
FeatureViewModel.kt
FeatureUiState.kt
FeatureRepository.kt
ActionFeatureUseCase.kt
```

Examples from this app:

```text
LoginScreen.kt
LoginViewModel.kt
LoginUiState.kt
AuthRepository.kt
LoginUseCase.kt
LiveDriveRepository.kt
ObserveAiResultsUseCase.kt
```

## Import And Dependency Rules

- Presentation can depend on domain, data only through ViewModel construction if the existing feature already does this manually, and core UI/navigation helpers.
- Domain can depend on data repository interfaces/classes and model types.
- Data can depend on core network/config/database/location.
- Core must not depend on feature packages.
- Avoid circular dependencies between features.
- One feature should not import another feature's presentation classes except from navigation.

## Code Style Rules

- Keep Kotlin code simple and explicit.
- Use `val` by default.
- Use `private` for helpers and mutable state.
- Avoid large functions; split by responsibility.
- Avoid comments that only repeat code. Add comments only for non-obvious stream, camera, lifecycle, or network behavior.
- Preserve the existing package names.
- Preserve existing behavior unless the task explicitly asks for a change.
- Do not rename files, classes, routes, or public methods without updating all references.

## LLM Pre-Code Checklist

Before editing code, the LLM must answer these internally:

- Which feature owns this change?
- Does it need presentation, domain, data, or core?
- Is there an existing component/use case/repository to reuse?
- Will this change affect navigation routes?
- Will this change affect API DTOs or backend contracts?
- What state fields are needed for loading, error, success, and data?
- What resource needs cancellation or cleanup?
- What test or build command should be run after the edit?

## LLM Forbidden Moves

- Do not put API calls directly in Compose screens.
- Do not expose mutable state from ViewModels.
- Do not hardcode server URLs outside `AppConfig`.
- Do not create duplicate UI components when a shared component exists.
- Do not mix backend AI logic into Android UI.
- Do not change package names casually.
- Do not flatten features into one global `screens`, `viewmodels`, or `repositories` folder.
- Do not add a library for a small problem already solved by Kotlin, Compose, Retrofit, OkHttp, or existing code.
- Do not ignore stream cancellation.
- Do not swallow errors silently unless the existing UX intentionally allows it, and even then prefer a logged or state-based signal.

## Done Definition

A code change is complete only when:

- Files are in the correct feature/core folders.
- The MVVM flow is preserved.
- UI state is immutable and exposed as `StateFlow`.
- Network/stream work is handled in data/domain/ViewModel, not in composables.
- Errors and loading states are represented.
- Long-running jobs are cancelled correctly.
- Navigation still works.
- Shared styling is reused.
- The project builds or the reason it could not be built is documented.

