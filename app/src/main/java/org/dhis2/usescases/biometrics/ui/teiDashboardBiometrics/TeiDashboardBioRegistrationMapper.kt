package org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics

import org.dhis2.R
import org.dhis2.commons.biometrics.declinedButtonColor
import org.dhis2.commons.biometrics.defaultButtonColor
import org.dhis2.commons.biometrics.failedButtonColor
import org.dhis2.commons.biometrics.getBioIconFailed
import org.dhis2.commons.biometrics.getBioIconSuccess
import org.dhis2.commons.biometrics.successButtonColor
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.biometrics.RegisterResult

class TeiDashboardBioRegistrationMapper(
    val resourceManager: ResourceManager,
) {
    fun map(
        registerResult: RegisterResult?,
        actionCallback: () -> Unit,
    ): TeiDashboardBioModel {
        val statusModel = if (registerResult == RegisterResult.Failure) {
            BioStatus(
                text = getText(registerResult),
                backgroundColor = getBackgroundColor(registerResult) ?: defaultButtonColor,
                icon = getIcon(registerResult)
            )
        } else {
            null
        }

        val buttonModel = if (registerResult != RegisterResult.Failure) {
            BioButtonModel(
                text = getText(registerResult),
                backgroundColor = getBackgroundColor(registerResult),
                icon = getIcon(registerResult),
                onActionClick = actionCallback
            )
        } else {
            null
        }

        return TeiDashboardBioModel(
            statusModel = statusModel,
            buttonModel = buttonModel
        )
    }

    private fun getText(
        registerResult: RegisterResult?,
    ): String {
        return if (registerResult == null) {
            resourceManager.getString(R.string.enroll_biometrics)
        } else {
            when (registerResult) {
                is RegisterResult.Completed ->
                    resourceManager.getString(R.string.biometrics_completed)

                is RegisterResult.Failure ->
                    resourceManager.getString(R.string.biometrics_declined)

                is RegisterResult.PossibleDuplicates ->
                    resourceManager.getString(R.string.biometrics_declined)

                is RegisterResult.AgeGroupNotSupported -> resourceManager.getString(R.string.age_group_not_supported)
                is RegisterResult.RegisterLastFailure -> resourceManager.getString(R.string.enroll_biometrics)
            }
        }
    }

    private fun getIcon(
        registerResult: RegisterResult?,
    ): Int {
        return if (registerResult == null) {
            R.drawable.ic_bio_fingerprint
        } else {
            when (registerResult) {
                is RegisterResult.Completed -> resourceManager.context.getBioIconSuccess()
                is RegisterResult.Failure -> resourceManager.context.getBioIconFailed()
                is RegisterResult.PossibleDuplicates -> resourceManager.context.getBioIconFailed()
                is RegisterResult.AgeGroupNotSupported -> resourceManager.context.getBioIconFailed()
                is RegisterResult.RegisterLastFailure -> R.drawable.ic_bio_fingerprint
            }
        }
    }

    private fun getBackgroundColor(
        registerResult: RegisterResult?,
    ): String? {
        return if (registerResult == null) {
            null
        } else {
            when (registerResult) {
                is RegisterResult.Completed -> successButtonColor
                is RegisterResult.Failure -> declinedButtonColor
                is RegisterResult.PossibleDuplicates -> failedButtonColor
                is RegisterResult.AgeGroupNotSupported -> failedButtonColor
                is RegisterResult.RegisterLastFailure -> null
            }
        }
    }
}