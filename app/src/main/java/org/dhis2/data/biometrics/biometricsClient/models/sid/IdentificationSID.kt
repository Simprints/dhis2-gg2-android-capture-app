package org.dhis2.data.biometrics.biometricsClient.models.sid


data class IdentificationSID(
    val guid: String,
    val confidence: Float,
    val confidenceBand: String,
    val isLinkedToCredential: Boolean,
    val isVerified: Boolean
)