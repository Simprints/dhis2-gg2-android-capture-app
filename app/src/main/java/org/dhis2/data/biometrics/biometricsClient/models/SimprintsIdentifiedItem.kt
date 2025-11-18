package org.dhis2.data.biometrics.biometricsClient.models

data class SimprintsIdentifiedItem(
    val guid: String,
    val confidence: Float,
    val isLinkedToCredential: Boolean,
    val isVerified: Boolean?,
)