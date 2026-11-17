# EyeSeeTea SDK Configuration Guide

This guide explains how is configured the DHIS2 Android SDK in the app. The SDK fork by EyeSeeTea is published on JitPack and can also be used for local development.

---

## SDK Publication

The EyeSeeTea DHIS2 Android SDK is published on **JitPack**: https://jitpack.io/#EyeSeeTea/dhis2-android-sdk

**Available versions**: Tags (e.g., `v1.13.0-eyeseetea-fork-1`), commit SHA (e.g., `94ae031f2f`), or branches with `-SNAPSHOT` suffix.

Check build status at the JitPack URL: ✅ Green = ready, ⏳ Yellow = building, ❌ Red = failed.

---

## Configuring the App

### 1. Add JitPack Repository

In `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### 2. Add Dependency

In your module's `build.gradle.kts`, use the version catalog from `gradle/libs.versions.toml`:

```kotlin
dependencies {
    // Using version catalog (recommended)
    implementation(libs.dhis2.android.sdk)
}
```

**Note**: The version is configured in `gradle/libs.versions.toml`:
- `dhis2sdk = "94ae031f2f"` (or tag like `"v1.13.0-eyeseetea-fork-1"`)
- `dhis2-android-sdk = { group = "com.github.EyeSeeTea", name = "dhis2-android-sdk", version.ref = "dhis2sdk" }`

To change the version, update `dhis2sdk` in `gradle/libs.versions.toml`.

### 3. Configure Composite Build for Local Development

Add to `settings.gradle.kts`:

```kotlin
// EyeSeeTea customization - Composite Build: use local SDK or JitPack
fun readLocalProperty(key: String): String? {
    val localPropsFile = file("local.properties")
    if (localPropsFile.exists()) {
        val props = java.util.Properties()
        localPropsFile.inputStream().use { props.load(it) }
        return props.getProperty(key)
    }
    return null
}

val useLocalSdkFromGradle = providers.gradleProperty("dhis2.useLocalSdk").orNull
val useLocalSdkFromLocal = readLocalProperty("dhis2.useLocalSdk")
val useLocalSdk = (useLocalSdkFromGradle ?: useLocalSdkFromLocal)?.toBoolean() ?: false

val sdkPathFromGradle = providers.gradleProperty("dhis2.sdkPath").orNull
val sdkPathFromLocal = readLocalProperty("dhis2.sdkPath")
val sdkPathFromProps = sdkPathFromGradle ?: sdkPathFromLocal

val sdkPaths = listOfNotNull(
    sdkPathFromProps?.let { file(it) },
    file("../dhis2-android-sdk"),
    file("../../dhis2-android-sdk"),
    file(System.getProperty("user.home") + "/Workspace/dhis2-android-sdk"),
).firstOrNull { it.exists() && it.isDirectory && it.resolve("settings.gradle.kts").exists() }

if (useLocalSdk && sdkPaths != null) {
    println("🔗 Using local SDK from: ${sdkPaths.absolutePath}")
    includeBuild(sdkPaths) {
        dependencySubstitution {
            substitute(module("com.github.EyeSeeTea:dhis2-android-sdk"))
                .using(project(":core"))
        }
    }
} else {
    println("📦 Using JitPack")
}
```

### 4. Configure `local.properties`

Create or edit `local.properties` in the root of your app:

```properties
# Use local SDK (true) or JitPack (false)
dhis2.useLocalSdk=true

# Optional: Custom SDK path (relative or absolute)
# If not specified, searches in: ../dhis2-android-sdk, ../../dhis2-android-sdk, ~/Workspace/dhis2-android-sdk
dhis2.sdkPath=/Users/your-username/Workspace/dhis2-android-sdk
```

**Note**: `local.properties` is in `.gitignore` by default.

---

## How It Works

- **`dhis2.useLocalSdk=true`**: Searches for local SDK, uses Composite Build if found, compiles from source (changes immediate)
- **`dhis2.useLocalSdk=false`** or SDK not found: Uses JitPack automatically
  
The substitution uses `module("com.github.EyeSeeTea:dhis2-android-sdk")` **without version** so it matches whatever version is in `libs.versions.toml` (dhis2sdk).

The same dependency declaration works with both local SDK and JitPack.

### Verifying composite build is active

1. **At build time**: With `dhis2.useLocalSdk=true`, the first line of Gradle output should show `🔗 Using local SDK from: <path>`. If you see `📦 Using JitPack` or `📦 Local SDK not found`, the composite build is not active.
2. **Dependency report**: Run `./gradlew :app:dependencies --configuration releaseRuntimeClasspath` (or `debugRuntimeClasspath`) and check that `com.github.EyeSeeTea:dhis2-android-sdk` is resolved to a **project** (e.g. `project :core` from the included build), not to a JitPack artifact.


---

## Important: AGP Version Compatibility

**⚠️ Critical**: When using Composite Build, both app and SDK must use the **same Android Gradle Plugin version**.

If versions don't match, you'll see:
```
Using multiple versions of the Android Gradle Plugin [X.X.X, Y.Y.Y] across Gradle builds is not allowed.
```

**Solution**: match the app's AGP (`gradle` key in the app's `gradle/libs.versions.toml`)
to whatever AGP the SDK checkout is on — downgrade the **app**, not the SDK. The SDK
is a separate, independently-versioned project (its own Gradle wrapper, its own
Kotlin/KSP versions); bumping its tooling just to match the app risks breaking the
SDK's own build and touches a repo you may not intend to change at all. The app's
`libs.versions.toml` is a one-line, local-only edit (see below) that only affects
your machine.

**This downgrade is local-only, never committed.** It exists purely so your machine
can build the composite (local SDK + app) while the SDK's own AGP/Kotlin/KSP
versions are behind the app's. Revert it (or use `git stash`/a dedicated local
branch you never push) once you switch back to `dhis2.useLocalSdk=false`, and never
include it in a commit for the feature you are actually working on.

### Full fix, step by step (verified working)

Lowering the AGP version alone is usually **not sufficient**. AGP 9 bundles Kotlin
support "built-in", so any module that never explicitly declared the Kotlin Android
plugin (relying on that implicit AGP 9 behavior) will fail to compile once AGP drops
below 9, with errors like:

```
None of the following candidates is applicable:
    fun DependencyHandler.kotlin(module: String, version: String? = ...): Any
    fun PluginDependenciesSpec.kotlin(module: String): PluginDependencySpec
Unresolved reference 'compilerOptions'.
```

(this happens on any module with a `kotlin { ... }` or `kapt { ... }` block at the
top level of its `build.gradle.kts`).

1. **Lower the app's AGP** in `gradle/libs.versions.toml`: set `gradle = "<SDK's AGP version>"`
   (e.g. `"8.13.2"`) to match the SDK checkout's own AGP version (check the SDK's
   own `gradle/libs.versions.toml`).
2. **Add the Kotlin Android plugin explicitly** to every Android (non-KMP) module
   that has a top-level `kotlin { ... }` block and doesn't already declare
   `org.jetbrains.kotlin.android`:
   ```kotlin
   plugins {
       id("com.android.library") // or com.android.application
       id("org.jetbrains.kotlin.android")
       // ...
   }
   ```
   Use the plain `id(...)` form **without a version** — the root project's
   `buildscript { classpath(libs.kotlinPlugin) }` already puts the Kotlin Gradle
   plugin on the classpath without a pinned version for plugin-resolution purposes;
   declaring it again via a version-catalog `alias(...)` (which resolves to a
   specific version) collides with that and fails with *"the plugin is already on
   the classpath with an unknown version, so compatibility cannot be checked"*.
   As of this writing this applies to: `app`, `commons`, `dhis_android_analytics`,
   `dhis2_android_maps`, `compose-table`, `form`, `stock-usecase`,
   `dhis2-mobile-program-rules`. KMP modules (`sync`, `login`, `commonskmm`,
   `aggregates`, `tracker`) already bring their own Kotlin plugin via
   `org.jetbrains.kotlin.multiplatform` and do not need this.
3. **Replace `com.android.legacy-kapt` with `org.jetbrains.kotlin.kapt`** (same
   `id(...)`-without-version rule) in every module that uses kapt — AGP 9's
   `legacy-kapt` plugin is not compatible with AGP 8. As of this writing:
   `app`, `commons`, `dhis_android_analytics`, `dhis2_android_maps`.

If a module you haven't touched still fails after this, it likely has the same
`kotlin { ... }` pattern — apply step 2 to it as well and update the module list
above.

---

## Troubleshooting

### "Failed to resolve: com.github.EyeSeeTea:dhis2-android-sdk:android-core:..."

- Verify tag/commit exists at https://jitpack.io/#EyeSeeTea/dhis2-android-sdk
- Wait for JitPack to finish building (5-10 minutes)
- Check build logs in JitPack

### "Project with path ':core' could not be found"

- Verify SDK path in `local.properties` is correct
- Ensure SDK has `settings.gradle.kts` file

### Local SDK Not Detected

1. Check `local.properties` has `dhis2.useLocalSdk=true`
2. Verify SDK path is correct (relative to app root or absolute)
3. Ensure SDK directory exists and contains `settings.gradle.kts`

### "Using multiple versions of the Android Gradle Plugin"

- Update both `gradle/libs.versions.toml` files to use the same AGP version

### Verify What's Being Used

```bash
./gradlew tasks --console=plain | grep -i "using"
./gradlew :app:dependencies --configuration debugRuntimeClasspath | grep dhis2
```

---

## References

- **JitPack SDK**: https://jitpack.io/#EyeSeeTea/dhis2-android-sdk
- **JitPack Docs**: https://jitpack.io/docs/
- **Composite Build Docs**: https://docs.gradle.org/current/userguide/composite_builds.html
