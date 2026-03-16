# EyeSeeTea customizations (base flavor)

Differences of the **eyeseetea flavor** compared to **Oslo 3.3.1**.

## 1. Build and configuration

### 1.1 Product flavor «eyeseetea»

The **eyeseetea** flavor is added.

| Location | What |
|----------|------|
| `app/build.gradle.kts` | Block `productFlavors { create("eyeseetea") { applicationId = "com.eyeseetea.dhis2", dimension = "default", versionCode, versionName } }`. File header matches Oslo (no `@file:OptIn(KspExperimental::class)`). |
| `login/build.gradle.kts` | Same flavor in the login module |
| `app/src/eyeseetea/` | Flavor resources (strings, google-services.json, etc.) |
| `app/src/eyeseeteaDebug/`, `app/src/eyeseeteaRelease/` | Debug/release variants |

### 1.2 SDK: JitPack and composite build

Oslo uses the official SDK. Here the EyeSeeTea fork is used via JitPack and optionally a local SDK via composite build.

| Location | What |
|----------|------|
| `settings.gradle.kts` | DIFFERS. Logic for `dhis2.useLocalSdk` / `dhis2.sdkPath`, `includeBuild` for local SDK, module substitution for `com.github.EyeSeeTea:dhis2-android-sdk:android-core`. |
| `gradle/libs.versions.toml` | SDK version (EyeSeeTea commit/tag on JitPack) |
| `gradle.properties` | DIFFERS. `dhis2.useLocalSdk`, `dhis2.sdkPath` (if applicable). |
| `EyeSeeTea.md` (root) | EyeSeeTea SDK documentation |

### 1.3 Other build files that differ

- `app/build.gradle.kts`: only the eyeseetea flavor block (see 1.1); header matches Oslo.
- `login/build.gradle.kts`: DIFFERS (eyeseetea flavor).

## 2. Branding

The **eyeseetea** flavor sets app name and logo to «EyeSeeTea».

| Location | What |
|----------|------|
| `app/src/eyeseetea/res/values/strings.xml` (and locale variants) | `app_name`, `logo_text` = "EyeSeeTea" |
| `app/src/eyeseeteaDebug/res/values/strings.xml` (and variants) | `app_name` = "EyeSeeTea Debug", `logo_text` = "EyeSeeTea" |

---

## 3. 2FA and authentication errors (CommonsKMM)

In **DomainErrorMapper.kt** and **D2ErrorMessageProviderImpl.kt** this repo explicitly handles 2FA error codes; Oslo does not include them in those branches.

- **DomainErrorMapper.kt**: the 7 2FA codes (`INCORRECT_TWO_FACTOR_CODE`, `INCORRECT_TWO_FACTOR_CODE_TOTP`, `EMAIL_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_EMAIL`, `TWO_FACTOR_MANY_SEND_ATTEMPTS`, `SMS_TWO_FACTOR_CODE_SENT`, `INCORRECT_TWO_FACTOR_CODE_SMS`) are mapped to **AuthenticationError**. In Oslo they are not in that branch.
- **D2ErrorMessageProviderImpl.kt**: those 7 codes have a branch in the `when` that returns **defaultError()** (generic message). In Oslo that branch does not exist.

- **UserManagerImpl.java**: in `logIn()` the call is `d2.userModule().logIn(username, password, serverUrl, null)` (fourth parameter `null` = 2FA code). Oslo uses the 3-argument version without that parameter.

## 4. Only in this repository (not in Oslo)

- **CI/CD:** `.github/workflows/eyeseetea-main.yml`
