package org.dhis2.data.biometrics.biometricsClient.models

sealed class RegisterResult {
    data class Completed(val item: SimprintsRegisteredItem) : RegisterResult()
    data class PossibleDuplicates(val items: List<SimprintsIdentifiedItem>, val sessionId: String) :
        RegisterResult()

    data object Failure : RegisterResult()
    data object RegisterLastFailure : RegisterResult()
    data object AgeGroupNotSupported : RegisterResult()
}