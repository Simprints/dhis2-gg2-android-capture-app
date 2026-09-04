# EyeSeeTea customizations (base flavor)

Differences of the **eyeseetea flavor** compared to **Oslo 3.4.0**.

## 1. Build and configuration

### 1.1 Product flavor «eyeseetea»

The **eyeseetea** flavor is added.

| Location | What |
|----------|------|
| `app/build.gradle.kts` | Block `productFlavors { create("eyeseetea") { applicationId = "com.eyeseetea.dhis2", dimension = "default", versionCode, versionName } }`. |
| `login/build.gradle.kts` | Same flavor in the login module |
| `app/src/eyeseetea/` | Flavor resources (strings, google-services.json, etc.) |
| `app/src/eyeseeteaDebug/`, `app/src/eyeseeteaRelease/` | Debug/release variants |

### 1.2 SDK: JitPack and composite build

Oslo uses the official SDK coordinates. Here the EyeSeeTea fork is used via JitPack and optionally a local SDK via composite build.

| Location | What |
|----------|------|
| `settings.gradle.kts` | DIFFERS. Logic for `dhis2.useLocalSdk` / `dhis2.sdkPath`, `includeBuild` for local SDK, module substitution for `com.github.EyeSeeTea:dhis2-android-sdk`. |
| `gradle/libs.versions.toml` | SDK coordinates and version (`com.github.EyeSeeTea:dhis2-android-sdk`, `1.14.0-eyeseetea-fork-1`), plus EyeSeeTea app version name (`3.4.0-eyeseetea-fork-1`). |
| `gradle.properties` | DIFFERS. `dhis2.useLocalSdk`, `dhis2.sdkPath` (if applicable). |
| `EyeSeeTea.md` (root) | EyeSeeTea SDK documentation |

### 1.3 Other build files that differ

- `app/build.gradle.kts`: eyeseetea flavor block
- `login/build.gradle.kts`: eyeseetea flavor block

## 2. Branding

The **eyeseetea** flavor sets app name and logo to «EyeSeeTea».

| Location | What |
|----------|------|
| `app/src/eyeseetea/res/values/strings.xml` (and locale variants) | `app_name`, `logo_text` = "EyeSeeTea" |
| `app/src/eyeseeteaDebug/res/values/strings.xml` (and variants) | `app_name` = "EyeSeeTea Debug", `logo_text` = "EyeSeeTea" |

---

## 3. 2FA and authentication compatibility

The EyeSeeTea baseline keeps compatibility with SDK login methods and error handling that include 2FA-related codes. Oslo 3.4.0 does not include these branches in the same way.

- **DomainErrorMapper.kt**: the 7 2FA codes (`INCORRECT_TWO_FACTOR_CODE`, `INCORRECT_TWO_FACTOR_CODE_TOTP`, `EMAIL_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`, `TWO_FACTOR_MANY_SEND_ATTEMPTS`, `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_SMS`) are mapped to **AuthenticationError**.
- **D2ErrorMessageProviderImpl.kt**: those 7 codes have a branch in the `when` that returns **defaultError()**.
- **LoginRepositoryImpl.kt**: `blockingLogIn(username, password, serverUrl, null)` uses the SDK overload that accepts a fourth parameter for the 2FA code.
- **UserManagerImpl.java**: `logIn(username, password, serverUrl, null)` uses the SDK overload that accepts a fourth parameter for the 2FA code.

## 4. App update delivery method

The EyeSeeTea flavor uses the APK-file download update flow instead of the URL-based update flow used by `dhis2PlayServices`.

- **DownloadNewVersion.kt** in `app/src/eyeseetea/`: returns `DownloadMethod.File` after downloading the APK with `versionRepository.download(...)`.
- **Behavioral implication**: the app downloads the installation file locally and then continues with installation from that file, instead of only exposing an external URL.
- **Reference comparison**:
  - `app/src/eyeseetea/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt`
  - `app/src/dhis2PlayServices/java/org/dhis2/usescases/main/domain/DownloadNewVersion.kt`

## 5. Only in this repository (not in Oslo)

- **CI/CD:** `.github/workflows/eyeseetea-main.yml`

## 5. Oslo bug fixes active in this baseline

Patches for Oslo regressions that affect all forks. Each entry documents the ticket, affected version, fix location, and retirement condition.

| Fix | Ticket | Introduced | File | Retire when |
|-----|--------|------------|------|-------------|
| TEI search blank value filter | ANDROAPP-6844 | 3.3.0 | `SearchTEIViewModel.kt` — `updateQuery()` | Oslo fixes the empty-value guard in `updateQuery()` |
| "Mark as complete?" dialog always shown for completed events | ANDROAPP-7666 | 3.3.1 | `FormViewModel.kt` — `showDataEntryResultDialogDeprecated()`, `EventStatus.COMPLETED` branch | Oslo returns `FormActions.OnFinish` for completed events with no issues |

## 6. Extension points added for downstream flavors

Generic hooks added to shared code so flavors can attach their own behavior without
editing Oslo files. These are **not** flavor customizations: no flavor-specific logic
lives here, nothing references a client, and Oslo could adopt them unchanged. Each
flavor's *use* of a hook is documented in that flavor's own inventory.

### 6.1 `PostMetadataSyncAction` — work after a metadata sync

Status: `active` (added 3.4.1) — **currently on `feature-simprints/upgrade_3.4.1`, pending promotion to `develop-eyeseetea`**

| Location | What |
|----------|------|
| `commonskmm/src/commonMain/kotlin/org/dhis2/mobile/commons/domain/PostMetadataSyncAction.kt` | The contract. `fun interface` with `suspend operator fun invoke(): Result<Unit>`. Lives in `:commonskmm` because it is the only module both `:sync` and `:app` share. |
| `sync/src/commonMain/kotlin/org/dhis2/mobile/sync/domain/SyncMetadata.kt` | Third constructor parameter `postMetadataSyncActions: List<PostMetadataSyncAction> = emptyList()`, plus `runPostMetadataSyncActions()` invoked at the `input(50)` progress point. |
| `sync/src/androidMain/kotlin/org/dhis2/mobile/sync/di/SyncModule.android.kt` | `factoryOf(::SyncMetadata)` replaced by an explicit `factory { }` with `getOrNull() ?: emptyList()`. **Required:** `factoryOf` uses constructor reflection and ignores the default. |
| `app/src/main/java/org/dhis2/di/KoinInitialization.kt` | One line registering `postMetadataSyncModule`. Flavor-agnostic: each flavor source set declares that module, empty where unused. |

**Why it exists.** Until 3.4.1, flavors hooked extra sync work onto
`SyncPresenterImpl.syncMetadata()` in `:app` — Simprints refreshed the biometrics
configuration, WIDP synced notifications. Oslo 3.4.1 moved metadata sync into the KMP
`:sync` module and removed that seam. Nothing outside `:sync` can reach it: the module
only depends on `:commonskmm`, and its consumer `SyncMetadataWorker` injects the
concrete, `final` `SyncMetadata`, so a decorator registered in a flavor's DI is never
asked for. The extension point had to be added deliberately.

**Contract semantics.** Actions run sequentially in list order, only after the metadata
sync itself succeeded. A failing action — returned failure or thrown exception — is
logged and skipped; it never fails the sync and never blocks later actions, so one
flavor's broken action cannot break syncing for everyone.

**Design decision: one module per flavor, no shared overridable default.** Each flavor
source set declares its own `postMetadataSyncModule`, empty where unused (4 no-op files
today). The alternative — one shared default that flavors override — was tested and
rejected. Measured on Koin 4.1.1:

| Behavior | Result |
|---|---|
| Two definitions of the same type | **No exception.** Koin 4 dropped the per-definition `override` flag (it no longer compiles); `allowOverride` is on by default. |
| `modules(default, flavor)` | flavor wins |
| `modules(flavor, default)` | **default wins** |

The winner is decided by **load order in `KoinInitialization.kt`**, not by specificity —
Koin has no notion of one definition being more specific than another. Reordering that
list (an Oslo file that changes between versions) would silently stop a flavor's actions
from being registered, and the sync would still report success. The 4 no-op files cost
nothing at merge time: they sit at level 1 of the placement hierarchy, in source sets
Oslo never touches.

Also rejected: making the `KoinInitialization.kt` line conditional on
`BuildConfig.FLAVOR`. That removes the no-op files but puts flavor logic inside an Oslo
file (level 4) to save files that are already free (level 1).

**Tests:** `sync/src/commonTest/.../SyncMetadataTest.kt` covers ordering, no-run on sync
failure, and isolation for both a returned failure and a thrown exception.

**Consumers:** Simprints (biometrics configuration — see
`customizations/simprints/customization-files.md` §2.2). WIDP will need it for
notifications. Reusable write-up: technique **T2** in `customization-techniques.md`.
