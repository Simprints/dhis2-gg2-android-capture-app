package org.dhis2.data.biometrics.biometricsClient.models.sid

import com.google.gson.annotations.SerializedName

data class SubjectActionsSID(
    @field:SerializedName("events") val events: List<SubjectActionEventSID>?,
)

data class SubjectActionEventSID(
    @field:SerializedName("payload") val payload: SubjectActionPayloadSID?,
)

data class SubjectActionPayloadSID(
    @field:SerializedName("biometricReferences") val biometricReferences: List<BiometricReferenceSID>?,
)

data class BiometricReferenceSID(
    @field:SerializedName("type") val type: String,
    @field:SerializedName("format") val format: String,
    @field:SerializedName("templates") val templates: List<BiometricTemplateSID>,
)

data class BiometricTemplateSID(
    @field:SerializedName("template") val template: String,
)
