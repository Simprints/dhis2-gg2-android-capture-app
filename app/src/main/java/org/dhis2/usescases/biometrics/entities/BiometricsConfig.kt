package org.dhis2.usescases.biometrics.entities

enum class BiometricsMode {
    full,
    limited,
    zero
}

data class BiometricsConfig(
    val orgUnitGroup: String?,
    val projectId: String,
    val confidenceScoreFilter: Int?,
    val icon: String?,
    val lastVerificationDuration: Int?,
    val lastDeclinedEnrolDuration: Int?,
    val program: String?,
    val orgUnitLevelAsModuleId: Int?,
    val ageThresholdMonths: Int,
    val dateOfBirthAttribute: String,
    val biometricsMode: BiometricsMode
)