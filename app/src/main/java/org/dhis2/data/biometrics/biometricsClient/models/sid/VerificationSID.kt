package org.dhis2.data.biometrics.biometricsClient.models.sid

data class VerificationSID(
    val guid: String,
    val confidence: Float,
    val confidenceBand: String,
)

enum class ConfidenceBandSID {
    HIGH,
    MEDIUM,
    LOW,
    NONE,
}