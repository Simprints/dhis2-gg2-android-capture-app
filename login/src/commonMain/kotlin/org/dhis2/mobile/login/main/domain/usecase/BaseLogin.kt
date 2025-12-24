package org.dhis2.mobile.login.main.domain.usecase

import org.dhis2.mobile.login.main.data.LoginRepository
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.TwoFactorRequiredException

abstract class BaseLogin(
    val repository: LoginRepository,
) {
    suspend fun handleResult(
        result: Result<Unit>,
        serverUrl: String,
        username: String,
    ) = when {
        result.isSuccess -> {
            repository.unlockSession()
            if (result.isSuccess) {
                repository.updateAvailableUsers(username)
                repository.updateServerUrls(serverUrl)
                checkDeleteBiometrics()
            }
            LoginResult.Success(
                displayTrackingMessage = repository.displayTrackingMessage(),
                initialSyncDone =
                    repository.initialSyncDone(
                        serverUrl,
                        username,
                    ),
            )
        }

        else -> {
            when (val exception = result.exceptionOrNull()) {
                is TwoFactorRequiredException -> {
                    LoginResult.TwoFactorError(
                        type = exception.type,
                        message = exception.message,
                    )
                }
                else -> LoginResult.Error(exception?.message)
            }
        }
    }

    private suspend fun checkDeleteBiometrics() {
        if (repository.numberOfAccounts() >= 2) {
            repository.deleteBiometricCredentials()
        }
    }
}
