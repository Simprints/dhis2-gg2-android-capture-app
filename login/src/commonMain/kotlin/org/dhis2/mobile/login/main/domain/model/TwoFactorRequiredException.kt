package org.dhis2.mobile.login.main.domain.model

/**
 * EyeSeeTea customization - Exception thrown when 2FA is required during login
 * @param type The type of 2FA required (TOTP, EMAIL, SMS)
 * @param message Message from the repository (can be error or info message)
 */
class TwoFactorRequiredException(
    val type: TwoFactorType,
    val errorMessage: String? = null,
) : Exception("Two factor authentication required: $type")



