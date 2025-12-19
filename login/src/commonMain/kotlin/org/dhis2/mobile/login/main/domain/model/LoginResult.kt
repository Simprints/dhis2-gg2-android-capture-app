package org.dhis2.mobile.login.main.domain.model

sealed interface LoginResult {
    data class Success(
        val displayTrackingMessage: Boolean,
        val initialSyncDone: Boolean,
    ) : LoginResult

    data class Error(
        val message: String?,
    ) : LoginResult

    /**
     * EyeSeeTea customization - Two Factor Authentication Error
     * @param type The type of 2FA required (TOTP, EMAIL, SMS)
     * @param message Message from the repository (can be error or info message)
     */
    data class TwoFactorError(
        val type: TwoFactorType,
        val message: String? = null,
    ) : LoginResult
}
