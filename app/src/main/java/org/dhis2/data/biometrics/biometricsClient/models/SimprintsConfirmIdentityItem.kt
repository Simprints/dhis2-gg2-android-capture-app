package org.dhis2.data.biometrics.biometricsClient.models

data class SimprintsConfirmIdentityItem(
    val hasCredential: Boolean,
    val scannedCredential: ScannedCredential?
)
