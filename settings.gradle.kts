rootProject.name = "dhis2-android-capture-app"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://central.sonatype.com/repository/maven-snapshots")
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(
    ":app",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table", ":ui-components",
    ":stock-usecase"
)
include(":dhis2-mobile-program-rules")
include(":tracker")
include(":aggregates")
include(":commonskmm")
include(":login")
include(":sync")

// EyeSeeTea customization - Include SDK's modules
// 🔗 Composite Build: use local SDK local o JitPack according to gradle.properties or local.properties

// Helper function to read property from local.properties
fun readLocalProperty(key: String): String? {
    val localPropsFile = file("local.properties")
    if (localPropsFile.exists()) {
        val props = java.util.Properties()
        localPropsFile.inputStream().use { props.load(it) }
        return props.getProperty(key)
    }
    return null
}

// Read from gradle.properties first, then fallback to local.properties
val useLocalSdkFromGradle = providers.gradleProperty("dhis2.useLocalSdk").orNull
val useLocalSdkFromLocal = readLocalProperty("dhis2.useLocalSdk")
val useLocalSdk = (useLocalSdkFromGradle ?: useLocalSdkFromLocal)
    ?.toBoolean()
    ?: false  // by default use jitpack

val sdkPathFromGradle = providers.gradleProperty("dhis2.sdkPath").orNull
val sdkPathFromLocal = readLocalProperty("dhis2.sdkPath")
val sdkPathFromProps = sdkPathFromGradle ?: sdkPathFromLocal

val sdkPaths = listOfNotNull(
    sdkPathFromProps?.let { file(it) },  // 1. Path from gradle.properties
    file("../dhis2-android-sdk"),         // 2. Common relative path
    file("../../dhis2-android-sdk"),      // 3. alternative path
    file(System.getProperty("user.home") + "/Workspace/dhis2-android-sdk"), // 4. Common absolute patch
).firstOrNull { it.exists() && it.isDirectory && it.resolve("settings.gradle.kts").exists() }

if (useLocalSdk && sdkPaths != null) {
    println("🔗 Using local SDK from: ${sdkPaths.absolutePath}")
    includeBuild(sdkPaths) {
        dependencySubstitution {
            // Use group:name only (no version) so any version from libs.versions.toml is matched
            substitute(module("com.github.EyeSeeTea:dhis2-android-sdk"))
                .using(project(":core"))
        }
    }
} else {
    if (!useLocalSdk) {
        println("📦 Using JitPack (dhis2.useLocalSdk=false or not set in gradle.properties/local.properties)")
    } else {
        println("📦 Local SDK not found, using JitPack")
    }
}