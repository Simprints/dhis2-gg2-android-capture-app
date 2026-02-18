package org.dhis2.data.biometrics.biometricsClient

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.simprints.libsimprints.Constants
import com.simprints.libsimprints.Metadata
import com.simprints.libsimprints.SimHelper
import org.dhis2.R
import org.dhis2.commons.biometrics.BIOMETRICS_CONFIRM_IDENTITY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_ENROLL_LAST_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_ENROLL_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_IDENTIFY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_VERIFY_REQUEST
import org.dhis2.data.biometrics.biometricsClient.models.ConfirmIdentityResult
import org.dhis2.data.biometrics.biometricsClient.models.IdentifyResult
import org.dhis2.data.biometrics.biometricsClient.models.RegisterResult
import org.dhis2.data.biometrics.biometricsClient.models.ScannedCredential
import org.dhis2.data.biometrics.biometricsClient.models.SimprintsConfirmIdentityItem
import org.dhis2.data.biometrics.biometricsClient.models.SimprintsIdentifiedItem
import org.dhis2.data.biometrics.biometricsClient.models.SimprintsRegisteredItem
import org.dhis2.data.biometrics.biometricsClient.models.VerifyResult
import org.dhis2.data.biometrics.biometricsClient.models.sid.ConfidenceBandSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.IdentificationSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.RefusalFormSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.RegistrationSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.ScannedCredentialSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.VerificationSID
import timber.log.Timber

// TODO: This constants should be in libsimprints
private const val SIMPRINTS_HAS_CREDENTIALS = "hasCredential"
private const val SIMPRINTS_SCANNED_CREDENTIAL = "scannedCredential"

/*
When SIMPRINTS_HAS_CREDENTIALS is false,
Simprints ID expects a "versionCode" intent extra in order to return data encoded as JSON:
see https://github.com/Simprints/Android-Simprints-ID/blob/v2025.4.0/feature/client-api/src/main/java/com/simprints/feature/clientapi/mappers/response/LibSimprintsResponseMapper.kt#L62
LibSimprints include this as a constant since v2025.1.1:
see https://github.com/Simprints/LibSimprints/blob/v2025.1.1-SNAPSHOT/src/main/java/com/simprints/libsimprints/Constants.kt#L38
and also https://github.com/Simprints/LibSimprints/blob/v2025.2.1/src/main/java/com/simprints/libsimprints/contracts/VersionsList.kt#L15
However, LibSimprints v2025.1.1 minSdk is higher than DHIS2 3.2.1.1-simprints-fork-8 has.
These key and value are backported from the newer (unsupported) LibSimprints versions
to enable JSON formatting in data returned by Simprints ID.
 */
private const val SIMPRINTS_VERSION_CODE_KEY = "versionCode"
private const val SIMPRINTS_VERSION_CODE_VALUE_INITIAL_REWORK = 20250102
private const val SIMPRINTS_ENROLMENT_KEY = "enrolment"

class BiometricsClient(
    projectId: String,
    user: String,
    private val confidenceScoreFilter: Int,
    private val forkVersion: String
) {

    init {
        Timber.d("BiometricsClient!")
        Timber.d("userId: $user")
        Timber.d("projectId: $projectId")
        Timber.d("confidenceScoreFilter: $confidenceScoreFilter")
    }

    private val simHelper = SimHelper(projectId, user)

    fun register(
        activity: Activity,
        moduleId: String,
        ageInMonths: Long,
        trackedEntityInstanceUId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ) {
        Timber.d("Biometrics register!")
        Timber.d("moduleId: $moduleId")

        val metadata =
            createMetadata(
                trackedEntityInstanceUId,
                enrollingOrgUnitId,
                enrollingOrgUnitName,
                userOrgUnits,
                ageInMonths,
            )

        val intent = simHelper.register(moduleId, metadata)

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_ENROLL_REQUEST)
    }

    fun registerFromFragment(
        fragment: Fragment,
        moduleId: String,
        trackedEntityInstanceUId: String,
        ageInMonths: Long?,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ) {
        Timber.d("Biometrics register!")
        Timber.d("moduleId: $moduleId")

        val metadata =
            createMetadata(
                trackedEntityInstanceUId,
                enrollingOrgUnitId,
                enrollingOrgUnitName,
                userOrgUnits,
                ageInMonths
            )

        val intent = simHelper.register(moduleId, metadata)

        if (fragment.context != null) {
            launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_ENROLL_REQUEST)
        }
    }

    fun identify(activity: Activity, moduleId: String?, userOrgUnits: List<String>) {
        val finalModuleId = moduleId ?: DefaultModuleId

        Timber.d("Biometrics identify!")
        Timber.d("moduleId: $finalModuleId")

        val metadata = createMetadata(
            trackedEntityInstanceUId = null,
            enrollingOrgUnitId = null,
            enrollingOrgUnitName = null,
            userOrgUnits = userOrgUnits,
            ageInMonths = null
        )

        val intent = simHelper.identify(finalModuleId, metadata)

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_IDENTIFY_REQUEST)
    }

    fun verify(
        fragment: Fragment,
        guid: String,
        moduleId: String,
        trackedEntityInstanceUId: String,
        ageInMonths: Long? = null,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ) {

        if (guid == null) {
            Timber.i("Simprints Verification - Guid is Null - Please check again!")
            return
        }

        Timber.d("Biometrics verify!")
        Timber.d("moduleId: $moduleId")

        val metadata =
            createMetadata(
                trackedEntityInstanceUId,
                enrollingOrgUnitId,
                enrollingOrgUnitName,
                userOrgUnits,
                ageInMonths
            )

        val intent = simHelper.verify(moduleId, guid, metadata)

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
            val registrationJson = data.getStringExtra(SIMPRINTS_ENROLMENT_KEY)
            val registration: RegistrationSID? = registrationJson?.let {
                Gson().fromJson(it, RegistrationSID::class.java)
            }

            val hasCredential: Boolean? = data.getBooleanExtra(SIMPRINTS_HAS_CREDENTIALS, false)

            val scannedCredentialJson = data.getStringExtra(SIMPRINTS_SCANNED_CREDENTIAL)
            val scannedCredential: ScannedCredentialSID? = scannedCredentialJson?.let {
                Gson().fromJson(it, ScannedCredentialSID::class.java)
            }

            if (registration == null) {
                RegisterResult.Failure
            } else {
                RegisterResult.Completed(
                    SimprintsRegisteredItem(
                        guid = registration.guid,
                        hasCredential = hasCredential ?: false,
                        scannedCredential = if (scannedCredential == null) null else ScannedCredential(
                            scannedCredential.type,
                            scannedCredential.value
                        )
                    )
                )
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
                    val items = listOf<SimprintsIdentifiedItem>()

                    Timber.d("Possible duplicates but IdentifyResult is UserNotFound")
                    RegisterResult.PossibleDuplicates(
                        items,
                        identifyResponse.sessionId
                    )
                }

                is IdentifyResult.Failure -> {
                    RegisterResult.Failure
                }

                is IdentifyResult.AgeGroupNotSupported -> RegisterResult.AgeGroupNotSupported
            }
        }

        return if (biometricsCompleted) {
            when {
                data.hasExtra(Constants.SIMPRINTS_IDENTIFICATIONS) -> {
                    handlePossibleDuplicates()
                }

                data.hasExtra(SIMPRINTS_ENROLMENT_KEY) -> {
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

            val identificationsJson = data.getStringExtra(Constants.SIMPRINTS_IDENTIFICATIONS)
            val identifications: List<IdentificationSID>? = identificationsJson?.let {
                Gson().fromJson(it, Array<IdentificationSID>::class.java)?.toList()
            }

            val refusalFormJson = data.getStringExtra(Constants.SIMPRINTS_REFUSAL_FORM)
            val refusalForm: RefusalFormSID? = refusalFormJson?.let {
                Gson().fromJson(it, RefusalFormSID::class.java)
            }

            val sessionId: String = data.getStringExtra(Constants.SIMPRINTS_SESSION_ID) ?: ""

            val scannedCredentialJson = data.getStringExtra(SIMPRINTS_SCANNED_CREDENTIAL)
            val scannedCredential: ScannedCredentialSID? = scannedCredentialJson?.let {
                Gson().fromJson(it, ScannedCredentialSID::class.java)
            }

            return if (identifications == null && refusalForm != null) {
                IdentifyResult.BiometricsDeclined
            } else if (identifications.isNullOrEmpty()) {
                IdentifyResult.UserNotFound(sessionId)
            } else {
                val finalIdentifications =
                    identifications.filter { it.confidence >= confidenceScoreFilter && !it.isLinkedToCredential } +
                            identifications.filter { it.isLinkedToCredential }

                if (finalIdentifications.isEmpty()) {
                    Timber.w("Identify returns data but no match with confidence score filter")
                    IdentifyResult.UserNotFound(sessionId)
                } else {
                    IdentifyResult.Completed(
                        finalIdentifications.map {
                            SimprintsIdentifiedItem(
                                it.guid,
                                it.confidence,
                                it.isLinkedToCredential,
                                it.isVerified
                            )
                        },
                        sessionId,
                        scannedCredential = if (scannedCredential == null) null else ScannedCredential(
                            scannedCredential.type,
                            scannedCredential.value
                        )
                    )
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
            getVerificationJudgementBySimprints(data) ?: getVerificationJudgementByDhis2(data)

        } else {
            VerifyResult.Failure
        }
    }

    fun handleConfirmIdentityResponse(resultCode: Int, data: Intent?): ConfirmIdentityResult {
        Timber.d("Result code: $resultCode")

        if (data == null) {
            return ConfirmIdentityResult.Completed
        }

        val hasCredential: Boolean? = data.getBooleanExtra(SIMPRINTS_HAS_CREDENTIALS, false)

        val scannedCredentialJson = data.getStringExtra(SIMPRINTS_SCANNED_CREDENTIAL)
        val scannedCredential: ScannedCredentialSID? = scannedCredentialJson?.let {
            Gson().fromJson(it, ScannedCredentialSID::class.java)
        }

        return if (hasCredential == true && scannedCredential != null) {
            ConfirmIdentityResult.CompletedWithCredentials(
                SimprintsConfirmIdentityItem(
                    hasCredential = hasCredential,
                    scannedCredential = ScannedCredential(
                        scannedCredential.type,
                        scannedCredential.value
                    )
                )
            )
        } else {
            ConfirmIdentityResult.Completed
        }
    }


    fun confirmIdentify(
        activity: Activity,
        sessionId: String,
        guid: String,
        trackedEntityInstanceUId: String
    ) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("sessionId: $sessionId")
        Timber.d("guid: $guid")
        Timber.d("$SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID: $trackedEntityInstanceUId")

        val metadata = Metadata().put(SIMPRINTS_FORK_VERSION, forkVersion)
            .put(SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceUId)

        val intent = simHelper.confirmIdentity(sessionId, guid).putExtra(
            Constants.SIMPRINTS_METADATA, metadata.toString()
        )

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
    }

    fun confirmIdentify(
        fragment: Fragment,
        sessionId: String,
        guid: String,
        trackedEntityInstanceUId: String
    ) {
        Timber.d("Biometrics confirmIdentify!")
        Timber.d("sessionId: $sessionId")
        Timber.d("guid: $guid")
        Timber.d("$SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID: $trackedEntityInstanceUId")

        val metadata = Metadata().put(SIMPRINTS_FORK_VERSION, forkVersion)
            .put(SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceUId)

        val intent = simHelper.confirmIdentity(sessionId, guid).putExtra(
            Constants.SIMPRINTS_METADATA, metadata.toString()
        )

        launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
    }

    /*    fun noneSelected(activity: Activity, sessionId: String) {
            Timber.d("Biometrics confirmIdentify!")
            Timber.d("sessionId: $sessionId")
            Timber.d("guid: none_selected")

            val intent = simHelper.confirmIdentity(sessionId, "none_selected")

            launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_CONFIRM_IDENTITY_REQUEST)
        }*/

    fun registerLast(
        activity: Activity,
        sessionId: String,
        moduleId: String?,
        ageInMonths: Long? = null,
        trackedEntityInstanceUId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ) {
        val intent = createRegisterLastIntent(
            moduleId,
            sessionId,
            ageInMonths,
            trackedEntityInstanceUId,
            enrollingOrgUnitId,
            enrollingOrgUnitName,
            userOrgUnits
        )

        launchSimprintsAppFromActivity(activity, intent, BIOMETRICS_ENROLL_LAST_REQUEST)
    }

    fun registerLastFromFragment(
        fragment: Fragment, sessionId: String,
        moduleId: String?,
        ageInMonths: Long? = null,
        trackedEntityInstanceUId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ) {

        val intent = createRegisterLastIntent(
            moduleId,
            sessionId,
            ageInMonths,
            trackedEntityInstanceUId,
            enrollingOrgUnitId,
            enrollingOrgUnitName,
            userOrgUnits
        )

        launchSimprintsAppFromFragment(fragment, intent, BIOMETRICS_ENROLL_LAST_REQUEST)
    }

    private fun createRegisterLastIntent(
        moduleId: String?,
        sessionId: String,
        ageInMonths: Long?,
        trackedEntityInstanceUId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>
    ): Intent {
        val finalModuleId = moduleId ?: DefaultModuleId

        Timber.d("Biometrics confirmIdentify!")
        Timber.d("moduleId: $finalModuleId")
        Timber.d("sessionId: $sessionId")

        val metadata =
            createMetadata(
                trackedEntityInstanceUId,
                enrollingOrgUnitId,
                enrollingOrgUnitName,
                userOrgUnits,
                ageInMonths
            )

        val intent = simHelper.registerLastBiometrics(finalModuleId, sessionId, metadata)

        return intent
    }

    private fun checkBiometricsCompleted(data: Intent) =
        data.getBooleanExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, false)

    private fun getVerificationJudgementBySimprints(data: Intent): VerifyResult? {
        val existVerificationJudgement =
            data.extras?.containsKey(Constants.SIMPRINTS_VERIFICATION_SUCCESS)

        if (existVerificationJudgement == true) {
            val verificationSuccess =
                data.getBooleanExtra(Constants.SIMPRINTS_VERIFICATION_SUCCESS, false)

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
        val verificationJson = data.getStringExtra(Constants.SIMPRINTS_VERIFICATION)
        val verification: VerificationSID? = verificationJson?.let {
            Gson().fromJson(it, VerificationSID::class.java)
        }
        val confidenceBand = verification?.let {
            runCatching {
                ConfidenceBandSID.valueOf(verification.confidenceBand)
            }.getOrElse {
                Timber.e("Verify returns data with unsupported confidence band: ${verification.confidenceBand}")
                null
            }
        }

        return if (confidenceBand != null) {
            when (confidenceBand) {
                ConfidenceBandSID.HIGH, ConfidenceBandSID.MEDIUM, ConfidenceBandSID.LOW -> {
                    if (verification.confidence >= confidenceScoreFilter) {
                        VerifyResult.Match
                    } else {
                        Timber.w("Verify returns data but no match with confidence score filter")
                        VerifyResult.NoMatch
                    }
                }

                ConfidenceBandSID.NONE -> VerifyResult.NoMatch
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
            activity.startActivityForResult(intent.addJsonSupportToSimprintsResponse(), requestCode)
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
            fragment.startActivityForResult(intent.addJsonSupportToSimprintsResponse(), requestCode)
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

    private fun Intent.addJsonSupportToSimprintsResponse(): Intent =
        putExtra(SIMPRINTS_VERSION_CODE_KEY, SIMPRINTS_VERSION_CODE_VALUE_INITIAL_REWORK)

    private fun createMetadata(
        trackedEntityInstanceUId: String?,
        enrollingOrgUnitId: String?,
        enrollingOrgUnitName: String?,
        userOrgUnits: List<String>,
        ageInMonths: Long?,

        ): Metadata {

        val metadata = Metadata().put(SIMPRINTS_FORK_VERSION, forkVersion)

        val metadataWithTei = if (trackedEntityInstanceUId != null) {
            metadata.put(SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID, trackedEntityInstanceUId)
        } else {
            metadata
        }

        val metadataWithOrgUnitId = if (enrollingOrgUnitId != null) {
            metadataWithTei.put(SIMPRINTS_ENROLLING_ORG_UNIT_ID, enrollingOrgUnitId)
        } else {
            metadataWithTei
        }

        val metadataWithOrgUnitName = if (enrollingOrgUnitName != null) {
            metadataWithOrgUnitId.put(SIMPRINTS_ENROLLING_ORG_UNIT_NAME, enrollingOrgUnitName)
        } else {
            metadataWithOrgUnitId
        }

        val metadataWithUserOrgUnits = if (userOrgUnits.isNotEmpty()) {
            metadataWithOrgUnitName.put(SIMPRINTS_USER_UNITS, userOrgUnits.joinToString(","))
        } else {
            metadataWithOrgUnitName
        }

        val metadataWithAge = if (ageInMonths != null) {
            metadataWithUserOrgUnits.put(SIMPRINTS_SUBJECT_AGE, ageInMonths)
        } else {
            metadataWithUserOrgUnits
        }

        printMetadata(metadataWithAge)

        return metadataWithAge
    }

    private fun printMetadata(metadata: Metadata) {
        Timber.d("metadata: $metadata")
    }

    companion object {
        const val DefaultModuleId = "NA"

        const val SIMPRINTS_TRACKED_ENTITY_INSTANCE_ID = "trackedEntityInstanceId"
        const val SIMPRINTS_ENROLLING_ORG_UNIT_ID = "enrollingOrgUnitId"
        const val SIMPRINTS_ENROLLING_ORG_UNIT_NAME = "enrollingOrgUnitName"
        const val SIMPRINTS_USER_UNITS = "userOrgUnits"
        const val SIMPRINTS_SUBJECT_AGE = "subjectAge"
        const val SIMPRINTS_FORK_VERSION = "forkVersion"
    }
}