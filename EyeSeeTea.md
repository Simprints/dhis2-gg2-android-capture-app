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
            substitute(module("com.github.EyeSeeTea:dhis2-android-sdk:android-core"))
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

The same dependency declaration works with both local SDK and JitPack.

---

## Important: AGP Version Compatibility

**⚠️ Critical**: When using Composite Build, both app and SDK must use the **same Android Gradle Plugin version**.

If versions don't match, you'll see:
```
Using multiple versions of the Android Gradle Plugin [X.X.X, Y.Y.Y] across Gradle builds is not allowed.
```

**Solution**: Ensure both `gradle/libs.versions.toml` files have the same AGP version (e.g., `gradle = "8.9.3"`).

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
