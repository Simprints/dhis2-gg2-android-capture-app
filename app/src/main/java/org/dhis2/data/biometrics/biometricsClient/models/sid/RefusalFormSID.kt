package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class RefusalFormSID(
    @field:SerializedName("reason") val reason: String,
    @field:SerializedName("extra") val extra: String,
)
