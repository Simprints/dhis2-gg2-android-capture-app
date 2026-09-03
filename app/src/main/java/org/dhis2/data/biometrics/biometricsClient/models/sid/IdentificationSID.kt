package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class IdentificationSID(
    @field:SerializedName("guid") val guid: String,
    @field:SerializedName("confidence") val confidence: Float,
    @field:SerializedName("confidenceBand") val confidenceBand: String,
    @field:SerializedName("isLinkedToCredential") val isLinkedToCredential: Boolean,
    @field:SerializedName("isVerified") val isVerified: Boolean,
)