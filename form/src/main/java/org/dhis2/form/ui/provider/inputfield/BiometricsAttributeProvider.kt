package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import org.dhis2.form.model.biometrics.BiometricsAttributeUiModelImpl
import org.dhis2.form.ui.biometrics.components.biometricsTEIRegistration.BiometricsTEIRegistration

@Composable
internal fun ProvideBiometricsAttribute(fieldUiModel: BiometricsAttributeUiModelImpl) {
    BiometricsTEIRegistration(
        value = fieldUiModel.value,
        ageUnderThreshold = fieldUiModel.ageUnderThreshold,
        enabled = fieldUiModel.editable,
        onBiometricsClick = fieldUiModel::onBiometricsClick,
        onSave = fieldUiModel::onSaveTEI,
        registerLastAndSave = fieldUiModel::registerLastAndSave,
    )
}
