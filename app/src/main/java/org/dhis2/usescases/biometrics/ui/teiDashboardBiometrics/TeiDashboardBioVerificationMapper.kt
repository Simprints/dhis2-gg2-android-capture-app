package org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics

import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager

import org.dhis2.data.biometrics.biometricsClient.models.VerifyResult

class TeiDashboardBioVerificationMapper(
    val resourceManager: ResourceManager,
) {
    fun map(
        verifyResult: VerifyResult?,
        actionCallback: () -> Unit,
    ): TeiDashboardBioModel {
        val buttonModel = if (verifyResult == null) {
            BioButtonModel(
                text = resourceManager.getString(R.string.biometrics_verification_not_done),
                backgroundColor = null,
                onActionClick = actionCallback
            )
        } else {
            if (verifyResult == VerifyResult.NoMatch) {
                BioButtonModel(
                    text = resourceManager.context.getString(R.string.reverify_biometrics),
                    backgroundColor = null,
                    onActionClick = actionCallback
                )
            } else {
                null
            }
        }

        return TeiDashboardBioModel(
            statusModel = verifyResult?.let {
                BioStatus(
                    text = getText(verifyResult),
                    backgroundColor = getBackgroundColor(verifyResult),
                )
            },
            buttonModel = buttonModel
        )
    }

    private fun getText(
        verifyResult: VerifyResult
    ): String {
        return when (verifyResult) {
            is VerifyResult.Match ->
                resourceManager.getString(R.string.biometrics_verified)

            is VerifyResult.NoMatch ->
                resourceManager.getString(R.string.verification_failed)

            is VerifyResult.Failure ->
                resourceManager.getString(R.string.verification_declined)

            is VerifyResult.AgeGroupNotSupported -> resourceManager.getString(R.string.age_group_not_supported)
        }
    }

    private fun getBackgroundColor(
        verifyResult: VerifyResult
    ): String {
        return when (verifyResult) {
            is VerifyResult.Match -> "#34835d"
            is VerifyResult.NoMatch -> "#e30613"
            is VerifyResult.Failure -> "#a6a5a4"
            is VerifyResult.AgeGroupNotSupported -> "#a6a5a4"
        }
    }
}
