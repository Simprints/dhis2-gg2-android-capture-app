package org.dhis2.mobile.login.main.domain.model

/**
 * EyeSeeTea customization - Two Factor Authentication State
 * Represents the different states of 2FA verification during login
 */
sealed class TwoFactorState(val code: String) {
    data class TotpVerification(private val totpCode: String) : TwoFactorState(totpCode)
    
    data class EmailVerification(
        private val emailCode: String,
        val resendEnabled: Boolean
    ) : TwoFactorState(emailCode)
    
    data class SmsVerification(
        private val smsCode: String,
        val resendEnabled: Boolean
    ) : TwoFactorState(smsCode)
}

/**
 * Type of 2FA required
 */
enum class TwoFactorType {
    TOTP,
    EMAIL,
    SMS
}



