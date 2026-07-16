package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class ScannedCredentialSID(
    @field:SerializedName("type") val type: String,
    @field:SerializedName("value") val value: String,
)