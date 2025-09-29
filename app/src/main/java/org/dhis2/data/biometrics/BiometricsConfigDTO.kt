package org.dhis2.data.biometrics

import kotlinx.serialization.Serializable

@Serializable
enum class BiometricsModeDTO {
    full,
    limited,
    zero
}

@Serializable
data class BiometricsConfigDTO(
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
    val biometricsMode: BiometricsModeDTO,
    val enableIdentificationForTET: String?
)