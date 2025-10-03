package org.dhis2.data.biometrics.biometricsClient.models

data class SimprintsRegisteredItem(
    val guid: String,
    val hasCredential: Boolean,
    val scannedCredential: BiometricsCredential?
)

data class BiometricsCredential(
    val credentialType: String,
    val value: String,
)
