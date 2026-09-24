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

**Distribution channel:** EyeSeeTea forks (`eyeseetea`, `oca`, and future client flavors) are published to Google Play by each project's PM — a manual process, not automated in this repo's CI. Because of that, `DownloadNewVersion.kt` in these flavors must use the `dhis2PlayServices` pattern (`versionRepository.getUrl()` → `DownloadMethod.Url`), not the `dhis2`/generic-Oslo pattern (`versionRepository.download()` → `DownloadMethod.File`): once installed from Play, the app should not race Play Store's own update mechanism by downloading and side-loading its own APK. This is **not a client customization** — it is the correct default for any flavor distributed through Play, same as `dhis2PlayServices` itself. A new flavor's `DownloadNewVersion.kt` should be copied from `dhis2PlayServices`, not from `dhis2` or `eyeseetea`'s sibling flavors, unless that specific fork is confirmed to not use Play Store.

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

## 4. Only in this repository (not in Oslo)

- **CI/CD:** `.github/workflows/eyeseetea-main.yml`

## 5. Oslo bug fixes active in this baseline

Patches for Oslo regressions that affect all forks. Each entry documents the ticket, affected version, fix location, and retirement condition.

| Fix | Ticket | Introduced | File | Retire when |
|-----|--------|------------|------|-------------|
| TEI search blank value filter | ANDROAPP-6844 | 3.3.0 | `SearchTEIViewModel.kt` — `updateQuery()` | Oslo fixes the empty-value guard in `updateQuery()` |
| "Mark as complete?" dialog always shown for completed events | ANDROAPP-7666 | 3.3.1 | `FormViewModel.kt` — `showDataEntryResultDialogDeprecated()`, `EventStatus.COMPLETED` branch | Oslo returns `FormActions.OnFinish` for completed events with no issues |

## 5.1 Post-metadata-sync actions extension point

The `PostMetadataSyncAction` mechanism (see `eyeseetea-docs/customization-techniques.md` — T2) is
baseline infrastructure. It exists so a fork can run extra work after a metadata sync without
touching `:sync`, which cannot depend on `:app`.

| Location | What |
|----------|------|
| `commonskmm/src/commonMain/.../domain/PostMetadataSyncAction.kt` | The contract: `fun interface PostMetadataSyncAction { suspend operator fun invoke(): Result<Unit> }`. |
| `sync/src/commonMain/.../domain/SyncMetadata.kt` | Takes `postMetadataSyncActions: List<PostMetadataSyncAction> = emptyList()` and runs them sequentially at `input(50)`, after the sync itself succeeds. Failures are logged and swallowed — an action can never fail the metadata sync. |
| `sync/src/androidMain/.../di/SyncModule.android.kt` | Explicit `factory { }` (not `factoryOf(::SyncMetadata)`) with `getOrNull() ?: emptyList()`, so a flavor with no actions still resolves. |
| `app/src/main/.../di/KoinInitialization.kt` | One flavor-agnostic line: `postMetadataSyncModule` in the `modules(...)` list. |
| `app/src/<flavor>/.../di/PostMetadataSyncModule.kt` | Fork-owned. Must exist in every flavor source set — empty (`val postMetadataSyncModule = module { }`) where the flavor has nothing to run. |

No flavor in this baseline currently registers an action; the file exists in every flavor source
set as an empty module so `KoinInitialization` can register it unconditionally.
