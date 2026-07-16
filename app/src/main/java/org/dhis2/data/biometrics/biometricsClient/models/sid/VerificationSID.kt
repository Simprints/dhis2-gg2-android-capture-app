package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class VerificationSID(
    @field:SerializedName("guid") val guid: String,
    @field:SerializedName("confidence") val confidence: Float,
    @field:SerializedName("confidenceBand") val confidenceBand: String,
)

enum class ConfidenceBandSID {
    HIGH,
    MEDIUM,
    LOW,
    NONE,
}