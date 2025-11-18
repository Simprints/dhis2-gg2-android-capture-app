package org.dhis2.data.biometrics.biometricsClient.models


sealed class IdentifyResult {
    data class Completed(val items: List<SimprintsIdentifiedItem>, val sessionId: String) : IdentifyResult()
    data object BiometricsDeclined : IdentifyResult()
    data class UserNotFound(val sessionId: String) : IdentifyResult()
    data object Failure : IdentifyResult()
    data object AgeGroupNotSupported : IdentifyResult()

}