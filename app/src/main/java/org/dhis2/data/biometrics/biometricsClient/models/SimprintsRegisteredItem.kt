package org.dhis2.data.biometrics.biometricsClient.models

data class SimprintsRegisteredItem(
    val guid: String,
    val hasCredential: Boolean,
    val scannedCredential: ScannedCredential?
)

data class ScannedCredential(
    val type: String,
    val value: String,
)
