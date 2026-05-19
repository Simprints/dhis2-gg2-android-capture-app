package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class RegistrationSID(
    @field:SerializedName("guid") val guid: String,
)
