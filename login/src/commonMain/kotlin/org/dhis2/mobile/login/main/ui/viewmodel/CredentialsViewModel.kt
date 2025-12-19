package org.dhis2.mobile.login.main.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.commons.extensions.launchUseCase
import org.dhis2.mobile.commons.extensions.withMinimumDuration
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.login.main.domain.model.LoginResult
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.domain.model.TwoFactorState
import org.dhis2.mobile.login.main.domain.model.TwoFactorType
import org.dhis2.mobile.login.main.domain.usecase.BiometricLogin
import org.dhis2.mobile.login.main.domain.usecase.GetAvailableUsernames
import org.dhis2.mobile.login.main.domain.usecase.GetBiometricInfo
import org.dhis2.mobile.login.main.domain.usecase.GetHasOtherAccounts
import org.dhis2.mobile.login.main.domain.usecase.LogOutUser
import org.dhis2.mobile.login.main.domain.usecase.LoginUser
import org.dhis2.mobile.login.main.domain.usecase.OpenIdLogin
import org.dhis2.mobile.login.main.domain.usecase.UpdateBiometricPermission
import org.dhis2.mobile.login.main.domain.usecase.UpdateTrackingPermission
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.mobile.login.main.ui.state.AfterLoginAction
import org.dhis2.mobile.login.main.ui.state.CredentialsInfo
import org.dhis2.mobile.login.main.ui.state.CredentialsUiState
import org.dhis2.mobile.login.main.ui.state.LoginState
import org.dhis2.mobile.login.main.ui.state.OidcInfo
import org.dhis2.mobile.login.main.ui.state.ServerInfo
import org.dhis2.mobile.login.pin.domain.usecase.ForgotPinUseCase
import org.dhis2.mobile.login.pin.domain.usecase.GetIsSessionLockedUseCase

class CredentialsViewModel(
    private val navigator: Navigator,
    private val getAvailableUsernames: GetAvailableUsernames,
    private val getBiometricInfo: GetBiometricInfo,
    private val getHasOtherAccounts: GetHasOtherAccounts,
    private val loginUser: LoginUser,
    private val logOutUser: LogOutUser,
    private val biometricLogin: BiometricLogin,
    private val openIdLogin: OpenIdLogin,
    private val updateTrackingPermission: UpdateTrackingPermission,
    private val updateBiometricPermission: UpdateBiometricPermission,
    networkStatusProvider: NetworkStatusProvider,
    private val serverName: String?,
    private val serverUrl: String,
    private val username: String?,
    private val allowRecovery: Boolean,
    private val getIsSessionLockedUseCase: GetIsSessionLockedUseCase,
    private val forgotPinUseCase: ForgotPinUseCase,
    private val oidcInfo: OidcInfo?,
    private val fromHome: Boolean,
) : ViewModel() {
    private val isNetworkOnline =
        networkStatusProvider.connectionStatus
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                false,
            )

    private val initialState =
        CredentialsUiState(
            serverInfo =
                ServerInfo(
                    serverName = serverName,
                    serverUrl = serverUrl,
                    username = username,
                ),
            credentialsInfo =
                CredentialsInfo(
                    username = username ?: "",
                    password = "",
                    availableUsernames = emptyList(),
                    usernameCanBeEdited = username == null,
                ),
            loginState = LoginState.Disabled,
            errorMessage = null,
            allowRecovery = false,
            canUseBiometrics = false,
            oidcInfo = null,
            afterLoginActions = emptyList(),
            hasOtherAccounts = false,
            isSessionLocked = false,
            displayBiometricsDialog = false,
            twoFactorState = null,
            twoFactorCode = "",
            infoMessage = null,
        )

    private var loginJob: Job? = null

    private val _credentialsScreenState = MutableStateFlow(initialState)
    val credentialsScreenState =
        _credentialsScreenState
            .onStart {
                loadData()
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = initialState,
            )

    private fun loadData() {
        launchUseCase {
            val biometricInfo = getBiometricInfo(serverUrl)

            _credentialsScreenState.emit(
                CredentialsUiState(
                    serverInfo =
                        ServerInfo(
                            serverName = serverName,
                            serverUrl = serverUrl,
                            username = username,
                        ),
                    credentialsInfo =
                        CredentialsInfo(
                            username = username ?: "",
                            password = "",
                            availableUsernames = getAvailableUsernames(),
                            usernameCanBeEdited = username == null,
                        ),
                    loginState = LoginState.Disabled,
                    errorMessage = null,
                    allowRecovery = allowRecovery,
                    canUseBiometrics = getBiometricInfo(serverUrl).canUseBiometrics,
                    oidcInfo = oidcInfo,
                    afterLoginActions = emptyList(),
                    hasOtherAccounts = getHasOtherAccounts(),
                    isSessionLocked = getIsSessionLockedUseCase(),
                    displayBiometricsDialog = biometricInfo.canUseBiometrics && !fromHome,
                    twoFactorState = null,
                    twoFactorCode = "",
                    infoMessage = null,
                ),
            )
        }
    }

    fun updateUsername(username: String) {
        _credentialsScreenState.update {
            it.copy(
                credentialsInfo =
                    it.credentialsInfo.copy(
                        username = username,
                    ),
                loginState =
                    if (username.isNotBlank() &&
                        it.credentialsInfo.password.isNotBlank()
                    ) {
                        LoginState.Enabled
                    } else {
                        LoginState.Disabled
                    },
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun updatePassword(password: String) {
        _credentialsScreenState.update {
            it.copy(
                credentialsInfo =
                    it.credentialsInfo.copy(
                        password = password,
                    ),
                loginState =
                    if (password.isNotBlank() &&
                        it.credentialsInfo.username.isNotBlank()
                    ) {
                        LoginState.Enabled
                    } else {
                        LoginState.Disabled
                    },
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    fun onLoginClicked() {
        startLoginJob {
            loginUser(
                serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                username = _credentialsScreenState.value.credentialsInfo.username,
                password = _credentialsScreenState.value.credentialsInfo.password,
                isNetworkAvailable = isNetworkOnline.value,
                twoFactorCode = _credentialsScreenState.value.twoFactorCode.takeIf {
                    _credentialsScreenState.value.twoFactorState != null
                },
            )
        }
    }

    fun onOpenIdLogin() {
        startLoginJob {
            openIdLogin(
                serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                isNetworkAvailable = isNetworkOnline.value,
                clientId = _credentialsScreenState.value.oidcInfo?.oidcClientId ?: "",
                redirectUri = _credentialsScreenState.value.oidcInfo?.oidcRedirectUri ?: "",
                discoveryUri = _credentialsScreenState.value.oidcInfo?.discoveryUri(),
                authorizationUri = _credentialsScreenState.value.oidcInfo?.authorizationUri(),
                tokenUrl = _credentialsScreenState.value.oidcInfo?.tokenUrl(),
            )
        }
    }

    private fun startLoginJob(loginCall: suspend () -> LoginResult) {
        _credentialsScreenState.update {
            it.copy(
                loginState = LoginState.Running,
            )
        }
        loginJob =
            launchUseCase {
                val result =
                    withMinimumDuration {
                        loginCall()
                    }
                handleLoginResult(result)
            }
        loginJob?.invokeOnCompletion {
            _credentialsScreenState.update {
                it.copy(
                    loginState = LoginState.Enabled,
                )
            }
        }
    }

    private suspend fun handleLoginResult(result: LoginResult) =
        when (result) {
            is LoginResult.Success -> {
                _credentialsScreenState.update {
                    it.copy(
                        afterLoginActions =
                            buildList {
                                if (result.displayTrackingMessage) {
                                    add(AfterLoginAction.DisplayTrackingMessage)
                                }
                                if (getBiometricInfo(serverUrl).displayBiometricsMessageAfterLogin) {
                                    add(AfterLoginAction.DisplayBiometricsMessage)
                                }
                                add(AfterLoginAction.NavigateToNextScreen(result.initialSyncDone))
                            },
                        twoFactorState = null,
                        twoFactorCode = "",
                        errorMessage = null, // EyeSeeTea customization - Clear error message on successful login
                        infoMessage = null, // EyeSeeTea customization - Clear info message on successful login
                    )
                }
            }

            is LoginResult.Error -> {
                _credentialsScreenState.update {
                    it.copy(
                        errorMessage = result.message,
                    )
                }
            }

            // EyeSeeTea customization - Two Factor Authentication required
            is LoginResult.TwoFactorError -> {
                _credentialsScreenState.update {
                    val code = it.twoFactorState?.code ?: ""

                    val newTwoFactorState = when (result.type) {
                        TwoFactorType.TOTP -> TwoFactorState.TotpVerification(code)
                        TwoFactorType.EMAIL -> TwoFactorState.EmailVerification(
                            code,
                            resendEnabled = true
                        )

                        TwoFactorType.SMS -> TwoFactorState.SmsVerification(
                            code,
                            resendEnabled = true
                        )
                    }
                    
                    // EyeSeeTea customization - Determine if message is error or info based on 2FA type
                    // EMAIL_TWO_FACTOR_CODE_SENT and SMS_TWO_FACTOR_CODE_SENT are info messages (blue)
                    // INCORRECT_TWO_FACTOR_CODE_* are error messages (red)
                    // For TOTP: show error only if field is already visible
                    val isInfoMessage = result.type == TwoFactorType.EMAIL || result.type == TwoFactorType.SMS
                    val shouldShowError = when {
                        isInfoMessage -> false // EMAIL/SMS code sent is always info
                        it.twoFactorState == null && result.type == TwoFactorType.TOTP -> false // First time TOTP, don't show error
                        else -> true // TOTP code incorrect or other errors
                    }
                    
                    it.copy(
                        twoFactorState = newTwoFactorState,
                        errorMessage = if (shouldShowError && result.message != null) result.message else null,
                        infoMessage = if (isInfoMessage && result.message != null) result.message else null,
                    )
                }
            }
        }

    fun cancelLogin() {
        loginJob?.cancel()
        launchUseCase {
            logOutUser.invoke()
        }
    }

    context(platformContext: PlatformContext)
    fun onBiometricsClicked() {
        // Cancel any previous biometric login attempt
        loginJob?.cancel()

        loginJob =
            launchUseCase {
                val result = biometricLogin()

                when {
                    result.isSuccess -> {
                        updatePassword(password = result.getOrNull() ?: "")
                        onLoginClicked()
                    }

                    else -> {
                        _credentialsScreenState.update {
                            it.copy(
                                errorMessage = result.exceptionOrNull()?.message,
                                displayBiometricsDialog = false,
                            )
                        }
                    }
                }
            }
    }

    fun onManageAccountsClicked() {
        launchUseCase {
            navigator.navigate(destination = LoginScreenState.Accounts)
        }
    }

    fun onRecoverAccountClicked() {
        launchUseCase {
            navigator.navigate(
                destination =
                    LoginScreenState.RecoverAccount(
                        selectedServer = serverUrl,
                    ),
            )
        }
    }

    fun onTrackingPermission(granted: Boolean) {
        launchUseCase {
            updateTrackingPermission(granted)
            _credentialsScreenState.update {
                it.copy(
                    afterLoginActions =
                        it.afterLoginActions.toMutableList().apply {
                            remove(AfterLoginAction.DisplayTrackingMessage)
                        },
                )
            }
        }
    }

    fun checkPrivacyPolicy() {
        launchUseCase {
            navigator.navigateToPrivacyPolicy()
        }
    }

    context(platformContext: PlatformContext)
    fun onEnableBiometrics(granted: Boolean) {
        launchUseCase {
            updateBiometricPermission(
                serverUrl,
                credentialsScreenState.value.credentialsInfo.username,
                credentialsScreenState.value.credentialsInfo.password,
                granted,
            )
            _credentialsScreenState.update {
                it.copy(
                    afterLoginActions =
                        it.afterLoginActions.toMutableList().apply {
                            remove(AfterLoginAction.DisplayBiometricsMessage)
                        },
                )
            }
        }
    }

    fun goToNextScreen(initialSyncDone: Boolean) {
        launchUseCase {
            if (initialSyncDone) {
                navigator.navigateToHome()
            } else {
                navigator.navigateToSync()
            }
        }
    }

    fun onPinUnlocked() {
        // Session unlocked successfully, update the state
        launchUseCase {
            _credentialsScreenState.update {
                it.copy(
                    isSessionLocked = false,
                )
            }
            navigator.navigateToHome()
        }
    }

    fun onPinDismissed() {
        // User dismissed the PIN dialog (forgot PIN)
        // Logout the user from the app and ask for the password
        launchUseCase {
            forgotPinUseCase()
            _credentialsScreenState.update {
                it.copy(
                    isSessionLocked = false,
                )
            }
        }
    }

    // EyeSeeTea customization - Two Factor Authentication methods
    fun updateTwoFactorCode(code: String) {
        _credentialsScreenState.update {
            val updatedState = when (val currentState = it.twoFactorState) {
                is TwoFactorState.TotpVerification -> {
                    TwoFactorState.TotpVerification(code)
                }

                is TwoFactorState.EmailVerification -> {
                    TwoFactorState.EmailVerification(code, currentState.resendEnabled)
                }

                is TwoFactorState.SmsVerification -> {
                    TwoFactorState.SmsVerification(code, currentState.resendEnabled)
                }

                null -> null
            }
            it.copy(
                twoFactorState = updatedState,
                twoFactorCode = code,
                loginState = if (code.isNotBlank() && it.credentialsInfo.username.isNotBlank() && it.credentialsInfo.password.isNotBlank()) {
                    LoginState.Enabled
                } else {
                    LoginState.Disabled
                },
            )
        }
    }

    fun onResendEmailTwoFactor() {
        val currentState =
            _credentialsScreenState.value.twoFactorState as? TwoFactorState.EmailVerification
        if (currentState?.resendEnabled == true) {
            // Disable resend for 30 seconds
            _credentialsScreenState.update {
                it.copy(
                    twoFactorState = currentState.copy(resendEnabled = false),
                )
            }
            // Re-login to trigger email resend
            startLoginJob {
                loginUser(
                    serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                    username = _credentialsScreenState.value.credentialsInfo.username,
                    password = _credentialsScreenState.value.credentialsInfo.password,
                    isNetworkAvailable = isNetworkOnline.value,
                    twoFactorCode = null, // No code to trigger resend
                )
            }
            // Re-enable after 30 seconds
            launchUseCase {
                kotlinx.coroutines.delay(30000)
                _credentialsScreenState.update {
                    val state = it.twoFactorState as? TwoFactorState.EmailVerification
                    if (state != null) {
                        it.copy(twoFactorState = state.copy(resendEnabled = true))
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun onResendSmsTwoFactor() {
        val currentState =
            _credentialsScreenState.value.twoFactorState as? TwoFactorState.SmsVerification
        if (currentState?.resendEnabled == true) {
            // Disable resend for 30 seconds
            _credentialsScreenState.update {
                it.copy(
                    twoFactorState = currentState.copy(resendEnabled = false),
                )
            }
            // Re-login to trigger SMS resend
            startLoginJob {
                loginUser(
                    serverUrl = _credentialsScreenState.value.serverInfo.serverUrl,
                    username = _credentialsScreenState.value.credentialsInfo.username,
                    password = _credentialsScreenState.value.credentialsInfo.password,
                    isNetworkAvailable = isNetworkOnline.value,
                    twoFactorCode = null, // No code to trigger resend
                )
            }
            // Re-enable after 30 seconds
            launchUseCase {
                kotlinx.coroutines.delay(30000)
                _credentialsScreenState.update {
                    val state = it.twoFactorState as? TwoFactorState.SmsVerification
                    if (state != null) {
                        it.copy(twoFactorState = state.copy(resendEnabled = true))
                    } else {
                        it
                    }
                }
            }
        }
    }
}
