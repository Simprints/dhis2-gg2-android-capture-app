package org.dhis2.data.biometrics.biometricsClient.models

sealed class ConfirmIdentityResult {
    data class CompletedWithCredentials(val item: SimprintsConfirmIdentityItem) : ConfirmIdentityResult()
    object Completed : ConfirmIdentityResult()

}