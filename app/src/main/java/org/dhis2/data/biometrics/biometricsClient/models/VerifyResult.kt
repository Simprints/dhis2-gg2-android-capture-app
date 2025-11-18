package org.dhis2.data.biometrics.biometricsClient.models

sealed class VerifyResult {
    data object Match : VerifyResult()
    data object NoMatch : VerifyResult()
    data object Failure : VerifyResult()
    data object AgeGroupNotSupported : VerifyResult()
}
