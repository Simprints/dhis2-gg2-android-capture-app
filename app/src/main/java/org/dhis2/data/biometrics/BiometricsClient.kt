package org.dhis2.data.biometrics

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.os.Parcelable
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Identification
import com.simprints.libsimprints.RefusalForm
import com.simprints.libsimprints.Registration
import com.simprints.libsimprints.SimHelper
import com.simprints.libsimprints.Tier
import com.simprints.libsimprints.Verification
import org.dhis2.R
import org.dhis2.commons.biometrics.BIOMETRICS_CONFIRM_IDENTITY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_ENROLL_LAST_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_ENROLL_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_IDENTIFY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_VERIFY_REQUEST
import timber.log.Timber

sealed class RegisterResult {
    data class Completed(val guid: String) : RegisterResult()
    data class PossibleDuplicates(val items: List<SimprintsItem>, val sessionId: String) : RegisterResult()
    data object Failure : RegisterResult()
    data object RegisterLastFailure : RegisterResult()
    data object AgeGroupNotSupported : RegisterResult()

}

data class SimprintsItem(
    val guid: String,
    val confidence: Float
)

sealed class IdentifyResult {
    data class Completed(val items: List<SimprintsItem>, val sessionId: String) : IdentifyResult()
    data object BiometricsDeclined : IdentifyResult()
    data class UserNotFound(val sessionId: String) : IdentifyResult()
    data object Failure : IdentifyResult()
    data object AgeGroupNotSupported : IdentifyResult()

}

sealed class VerifyResult {
    data object Match : VerifyResult()
    data object NoMatch : VerifyResult()
    data object Failure : VerifyResult()
    data object AgeGroupNotSupported : VerifyResult()
}


class BiometricsClient(
    projectId: String,
    userId: String,
    private val confidenceScoreFilter: Int
) {

    init {
        Timber.d("BiometricsClient!")
        Timber.d("userId: $userId")
        Timber.d("projectId: $projectId")
        Timber.d("confidenceScoreFilter: $confidenceScoreFilter")
    }

    private val simHelper = SimHelper(projectId, userId)
    private val defaultModuleId = "NA"

    fun register(activity: Activity, moduleId: String, ageInMonths: Long) {
        Timber.d("Biometrics register!")
        Timber.d("moduleId: $moduleId")
        Timber.d("subjectAge: $ageInMonths")

        val metadata = com.simprints.libsimprints.Metadata()
            .put("subjectAge", ageInMonths)

        val intent = simHelper.register(moduleId, metadata)

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_ENROLL_REQUEST)
    }

    fun registerFromFragment(fragment: Fragment, moduleId: String,  extras: Map<String, String>, ageInMonths: Long?) {
        Timber.d("Biometrics register!")
        Timber.d("moduleId: $moduleId")
        Timber.d("subjectAge: $ageInMonths")
        printExtras(extras)

        val intent = if (ageInMonths != null){
            val metadata =  com.simprints.libsimprints.Metadata()
                .put("subjectAge", ageInMonths)

            simHelper.register(moduleId, metadata)
        } else {
            simHelper.register(moduleId)
        }

        extras.forEach { intent.putExtra(it.key, it.value) }

        if (fragment.context != null) {
            launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_ENROLL_REQUEST)
        }
    }

    fun identify(activity: Activity, moduleId: String?) {
        val finalModuleId = moduleId ?: defaultModuleId

        Timber.d("Biometrics identify!")
        Timber.d("moduleId: $finalModuleId")

        val intent = simHelper.identify(finalModuleId)

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_IDENTIFY_REQUEST)
    }

    fun verify(fragment: Fragment,
               guid: String,
               moduleId: String, extras: Map<String, String>,
               ageInMonths: Long? = null) {

        if (guid == null) {
            Timber.i("Simprints Verification - Guid is Null - Please check again!")
            return
        }

        Timber.d("Biometrics verify!")
        Timber.d("moduleId: $moduleId")
        Timber.d("subjectAge: $ageInMonths")

        printExtras(extras)

        val intent = if (ageInMonths != null){
            val metadata =  com.simprints.libsimprints.Metadata()
                .put("subjectAge", ageInMonths)

           simHelper.verify(moduleId, guid, metadata)
        } else {
            simHelper.verify(moduleId, guid)
        }

        extras.forEach { intent.putExtra(it.key, it.value) }

        if (fragment.context != null) {
            launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_VERIFY_REQUEST)
        }
    }


    fun handleRegisterResponse(resultCode: Int, data: Intent): RegisterResult {
        Timber.d("Result code: $resultCode")

        if (resultCode != Activity.RESULT_OK) {
            return when (resultCode) {
                Constants.SIMPRINTS_AGE_GROUP_NOT_SUPPORTED -> RegisterResult.AgeGroupNotSupported
                Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED -> RegisterResult.RegisterLastFailure
                else -> RegisterResult.Failure
            }
        }

        val biometricsCompleted = checkBiometricsCompleted(data)

        val handleRegister = {
            val registration: Registration? =
                data.getParcelableExtra(Constants.SIMPRINTS_REGISTRATION)

            if (registration == null) {
                RegisterResult.Failure
            } else {
                RegisterResult.Completed(registration.guid)
            }
        }

        val handlePossibleDuplicates = {
            when (val identifyResponse = handleIdentifyResponse(resultCode, data)) {
                is IdentifyResult.Completed -> {
                    Timber.d("Possible duplicates: ${identifyResponse.items}")
                    RegisterResult.PossibleDuplicates(
                        identifyResponse.items,
                        identifyResponse.sessionId
                    )
                }

                is IdentifyResult.BiometricsDeclined -> {
                    RegisterResult.Failure
                }

                is IdentifyResult.UserNotFound -> {
                    val items = listOf<SimprintsItem>()

                    Timber.d("Possible duplicates but IdentifyResult is UserNotFound")
                    RegisterResult.PossibleDuplicates(
                        items,
                        identifyResponse.sessionId
                    )
                }

                is IdentifyResult.Failure -> {
                    RegisterResult.Failure
                }

                is IdentifyResult.AgeGroupNotSupported ->  RegisterResult.AgeGroupNotSupported
            }
        }

        return if (biometricsCompleted) {
            when {
                data.hasExtra(Constants.SIMPRINTS_IDENTIFICATIONS) -> {
                    handlePossibleDuplicates()
                }

                data.hasExtra(Constants.SIMPRINTS_REGISTRATION) -> {
                    handleRegister()
                }

                else -> {
                    RegisterResult.Failure
                }
            }
        } else {
            RegisterResult.Failure
        }
    }

    fun handleIdentifyResponse(resultCode: Int, data: Intent?): IdentifyResult {
        Timber.d("Result code: $resultCode")

        if (resultCode != Activity.RESULT_OK || data == null) {
            return if (resultCode == Constants.SIMPRINTS_AGE_GROUP_NOT_SUPPORTED)
                IdentifyResult.AgeGroupNotSupported
            else IdentifyResult.Failure
        }

        val biometricsCompleted = checkBiometricsCompleted(data)

        if (biometricsCompleted) {
            val identifications =
                data.extractParcelableArrayExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)
                    ?: data.extractParcelableArrayListExtra<Identification>(Constants.SIMPRINTS_IDENTIFICATIONS)

            val refusalForm: RefusalForm? =
                data.getParcelableExtra(Constants.SIMPRINTS_REFUSAL_FORM)

            val sessionId: String = data.getStringExtra(Constants.SIMPRINTS_SESSION_ID) ?: ""

            return if (identifications == null && refusalForm != null) {
                IdentifyResult.BiometricsDeclined
            } else if (identifications.isNullOrEmpty()) {
                IdentifyResult.UserNotFound(sessionId)
            } else {
                val finalIdentifications =
                    identifications.filter { it.confidence >= confidenceScoreFilter }

                if (finalIdentifications.isEmpty()) {
                    Timber.w("Identify returns data but no match with confidence score filter")
                    IdentifyResult.UserNotFound(sessionId)
                } else {
                    IdentifyResult.Completed(finalIdentifications.map { SimprintsItem(it.guid, it.confidence) }, sessionId)
                }
            }
        } else {
            return IdentifyResult.Failure
        }
    }

    fun handleVerifyResponse(resultCode: Int, data: Intent): VerifyResult {
        Timber.d("Result code: $resultCode")

        if (resultCode != Activity.RESULT_OK) {
            return if (resultCode == Constants.SIMPRINTS_AGE_GROUP_NOT_SUPPORTED)
                VerifyResult.AgeGroupNotSupported
            else VerifyResult.NoMatch
        }

        val biometricsCompleted = checkBiometricsCompleted(data)

        return if (biometricsCompleted) {
             getVerificationJudgementBySimprints(data) ?:getVerificationJudgementByDhis2(data)

        } else {
            VerifyResult.Failure
        }
    }

    fun confirmIdentify(
        activity: Activity,
        sessionId: String,
        guid: String,
        extras: Map<String, String>
    ) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("sessionId: $sessionId")
        Timber.d("guid: $guid")
        printExtras(extras)

        val intent = simHelper.confirmIdentity(sessionId, guid)

        extras.forEach { intent.putExtra(it.key, it.value) }

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
    }

    fun confirmIdentify(
        fragment: Fragment,
        sessionId: String,
        guid: String,
        extras: Map<String, String>
    ) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("sessionId: $sessionId")
        Timber.d("guid: $guid")
        printExtras(extras)

        val intent = simHelper.confirmIdentity(sessionId, guid)

        extras.forEach { intent.putExtra(it.key, it.value) }

        launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
    }

    fun noneSelected(activity: Activity, sessionId: String) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("sessionId: $sessionId")
        Timber.d("guid: none_selected")

        val intent = simHelper.confirmIdentity(sessionId, "none_selected")

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
    }

    fun registerLast(activity: Activity, sessionId: String) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("moduleId: $defaultModuleId")
        Timber.d("sessionId: $sessionId")

        val intent = simHelper.registerLastBiometrics(defaultModuleId, sessionId)

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_ENROLL_LAST_REQUEST)
    }

    fun registerLastFromFragment(fragment: Fragment, sessionId: String) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("moduleId: $defaultModuleId")
        Timber.d("sessionId: $sessionId")

        val intent = simHelper.registerLastBiometrics(defaultModuleId, sessionId)

        launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_ENROLL_LAST_REQUEST)
    }

    private fun checkBiometricsCompleted(data: Intent) =
        data.getBooleanExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, false)

    private fun getVerificationJudgementBySimprints(data: Intent): VerifyResult? {
        val existVerificationJudgement = data.extras?.containsKey(Constants.SIMPRINTS_VERIFICATION_SUCCESS)

        if (existVerificationJudgement == true) {
            val verificationSuccess = data.getBooleanExtra(Constants.SIMPRINTS_VERIFICATION_SUCCESS, false)

            if (verificationSuccess) {
                return VerifyResult.Match
            } else {
                return VerifyResult.NoMatch
            }
        } else {
            return null
        }
    }

    private fun getVerificationJudgementByDhis2(data: Intent): VerifyResult {
        val verification: Verification? =
            data.getParcelableExtra(Constants.SIMPRINTS_VERIFICATION)

        return if (verification != null) {
            when (verification.tier) {
                Tier.TIER_1, Tier.TIER_2, Tier.TIER_3, Tier.TIER_4 -> {
                    if (verification.confidence >= confidenceScoreFilter) {
                        VerifyResult.Match
                    } else {
                        Timber.w("Verify returns data but no match with confidence score filter")
                        VerifyResult.NoMatch
                    }
                }

                Tier.TIER_5 -> VerifyResult.NoMatch
            }
        } else {
            VerifyResult.Failure
        }
    }

    private fun launchSimprintsAppFromActivity(
        activity: Activity,
        intent: Intent,
        requestCode: Int
    ) {
        try {
            activity.startActivityForResult(intent, requestCode)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(activity, R.string.biometrics_download_app, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchSimprintsAppFromFragment(
        fragment: Fragment,
        intent: Intent,
        requestCode: Int
    ) {
        try {
            fragment.startActivityForResult(intent, requestCode)
        } catch (ex: ActivityNotFoundException) {
            fragment.context?.let {
                Toast.makeText(
                    it,
                    R.string.biometrics_download_app,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun printExtras(extras: Map<String, String>) {
        Timber.d(
            "extras: ${
                extras.entries.joinToString(
                    prefix = "[",
                    separator = ", ",
                    postfix = "]",
                    limit = 2,
                    truncated = "..."
                )
            }"
        )
    }

    companion object {
        const val SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID = "trackedEntityInstanceId"
    }
}

inline fun <reified T : Parcelable> Intent.extractParcelableArrayListExtra(
    key: String,
): List<T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        getParcelableArrayListExtra(key, T::class.java)

    else ->
        @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

inline fun <reified T : Parcelable> Intent.extractParcelableArrayExtra(
    key: String,
): List<out T>? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ->
        getParcelableArrayExtra(key, T::class.java)?.asList()

    else ->
        @Suppress("DEPRECATION") getParcelableArrayExtra(key)?.mapNotNull { it as? T }
            ?.toTypedArray()?.asList()
}