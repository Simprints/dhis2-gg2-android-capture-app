package org.dhis2.form.ui.biometrics.components.biometricsTEIRegistration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.commons.biometrics.BIOMETRICS_FAILURE_PATTERN
import org.dhis2.commons.biometrics.BIOMETRICS_SEARCH_PATTERN
import org.dhis2.commons.biometrics.declinedButtonColor
import org.dhis2.commons.biometrics.successButtonColor
import org.dhis2.form.R

enum class BiometricsTEIState {
    INITIAL,
    SUCCESS,
    FAILURE
}

@Composable
fun BiometricsTEIRegistration(
    value: String?,
    ageUnderThreshold: Boolean,
    enabled: Boolean,
    onBiometricsClick: () -> Unit,
    onSave: (removeBiometrics:Boolean) -> Unit,
    registerLastAndSave: (sessionId: String) -> Unit
) {
    var linkLastBiometrics by remember { mutableStateOf(true) }

    val biometricsSearchSessionId = remember(value) {
        if (!value.isNullOrEmpty()) {
            if (value.startsWith(BIOMETRICS_SEARCH_PATTERN)) {
                value.split("_")[2]
            } else {
                null
            }
        } else {
            null
        }

    }

    val biometricsState = remember(value) {
        if (!value.isNullOrEmpty()) {
            if (value.startsWith(BIOMETRICS_FAILURE_PATTERN)) {
                BiometricsTEIState.FAILURE
            } else if (value.startsWith(BIOMETRICS_SEARCH_PATTERN)) {
                BiometricsTEIState.INITIAL
            } else {
                BiometricsTEIState.SUCCESS
            }
        } else {
            BiometricsTEIState.INITIAL
        }
    }

    Column {
        if (biometricsSearchSessionId != null && !ageUnderThreshold) {
            LinkLastBiometricsCheckBox(
                value = linkLastBiometrics,
                enabled = enabled,
                onCheckedChange = { linkLastBiometrics = it }
            )
        }

        Spacer(modifier = Modifier.height(96.dp))

        if (linkLastBiometrics && biometricsSearchSessionId != null && !ageUnderThreshold) {
            LinkLastBiometricsNextButton(enabled = enabled) {
                registerLastAndSave(
                    biometricsSearchSessionId
                )
            }
        } else {
            if (!ageUnderThreshold) {
                RegistrationButton(
                    biometricsState = biometricsState,
                    enabled = enabled,
                    onBiometricsClick = onBiometricsClick
                )
            }

            if (biometricsState == BiometricsTEIState.SUCCESS) {
                SaveButton(
                    text = stringResource(R.string.biometrics_update_and_save),
                    enabled = enabled,
                    onClick = { onSave(false) }
                )
            }else {
                SaveButton(
                    text = stringResource(R.string.biometrics_save_without_biometrics),
                    enabled = enabled,
                    onClick = { onSave(true) }
                )
            }
        }
    }
}

@Composable
fun RegistrationButton(
    biometricsState: BiometricsTEIState,
    enabled: Boolean,
    onBiometricsClick: () -> Unit
) {

    when (biometricsState) {
        BiometricsTEIState.INITIAL -> {
            RegistrationResult(
                onBiometricsClick = onBiometricsClick,
                enabled = enabled,
                resultText = R.string.biometrics_register_and_save,
                resultColor = null,
                showRetake = false
            )
        }
        BiometricsTEIState.SUCCESS -> {
            BiometricsStatusFlag(
                text = stringResource(id = R.string.biometrics_completed),
                backgroundColor = successButtonColor
            )
        }
        BiometricsTEIState.FAILURE -> {
            BiometricsStatusFlag(
                text = stringResource(id = R.string.biometrics_declined),
                backgroundColor = declinedButtonColor
            )
        }
    }
}

@Preview(showBackground = true, name = "Initial After biometrics search")
@Composable
fun PreviewBiometricsTEIRegistrationInitialAfterSearch() {

    BiometricsTEIRegistration(
        value = BIOMETRICS_SEARCH_PATTERN,
        enabled = true,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { }
    )
}

@Preview(showBackground = true, name = "Initial State disabled")
@Composable
fun PreviewBiometricsTEIRegistrationInitialDisabled() {

    BiometricsTEIRegistration(
        value = null,
        enabled = false,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { }
    )
}

@Preview(showBackground = true, name = "Initial After biometrics search disabled")
@Composable
fun PreviewBiometricsTEIRegistrationInitialAfterSearchDisabled() {

    BiometricsTEIRegistration(
        value = BIOMETRICS_SEARCH_PATTERN,
        enabled = false,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { }
    )
}

@Preview(showBackground = true, name = "Initial State")
@Composable
fun PreviewBiometricsTEIRegistrationInitial() {

    BiometricsTEIRegistration(
        value = null,
        enabled = true,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { }
    )
}

@Preview(showBackground = true, name = "Success State")
@Composable
fun PreviewBiometricsTEIRegistrationSuccess() {
    BiometricsTEIRegistration(
        value = "927232-2-323-2-32-32-32",
        enabled = true,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { }
    )
}

@Preview(showBackground = true, name = "Failure State")
@Composable
fun PreviewBiometricsTEIRegistrationFailure() {
    BiometricsTEIRegistration(
        value = BIOMETRICS_FAILURE_PATTERN,
        enabled = true,
        ageUnderThreshold = false,
        onBiometricsClick = { },
        onSave = {},
        registerLastAndSave = { },
    )
}




