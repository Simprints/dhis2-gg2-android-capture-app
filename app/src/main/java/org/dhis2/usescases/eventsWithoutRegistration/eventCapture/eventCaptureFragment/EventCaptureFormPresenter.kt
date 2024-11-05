package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.eventCaptureFragment

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.data.dhislogic.AUTH_ALL
import org.dhis2.data.dhislogic.AUTH_UNCOMPLETE_EVENT
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FieldsWithErrorResult
import org.dhis2.form.data.FieldsWithWarningResult
import org.dhis2.form.data.MissingMandatoryResult
import org.dhis2.form.data.NotSavedResult
import org.dhis2.form.data.SuccessfulResult
import org.dhis2.form.model.EventMode
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.biometrics.BiometricsDataElementStatus
import org.dhis2.form.model.biometrics.BiometricsDataElementUiModelImpl
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.entities.BiometricsMode
import org.dhis2.usescases.biometrics.getOrgUnitAsModuleId
import org.dhis2.usescases.biometrics.isLastVerificationValid
import org.dhis2.usescases.eventsWithoutRegistration.EventIdlingResourceSingleton
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureContract
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.domain.ReOpenEventUseCase
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus
import org.hisp.dhis.android.core.event.EventNonEditableReason
import org.hisp.dhis.android.core.event.EventStatus
import org.jetbrains.annotations.Nullable

class EventCaptureFormPresenter(
    private val view: EventCaptureFormView,
    private val activityPresenter: EventCaptureContract.Presenter,
    private val d2: D2,
    private val eventUid: String,
    private val resourceManager: ResourceManager,
    private val reOpenEventUseCase: ReOpenEventUseCase,
    private val dispatcherProvider: DispatcherProvider,
    private val basicPreferenceProvider: BasicPreferenceProvider,
) {

    private var biometricsVerificationStatus: Int = -1
    private var biometricsGuid: String? = null
    private var teiOrgUnit: String? = null
    private var trackedEntityInstanceId: String? = null

    private var biometricsVerificationUiModel: BiometricsDataElementUiModelImpl? = null


    fun handleDataIntegrityResult(result: DataIntegrityCheckResult, eventMode: EventMode? = null) {
        when (result) {
            is FieldsWithErrorResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                result.fieldUidErrorList,
                result.mandatoryFields,
                result.warningFields,
                eventMode,
            )

            is FieldsWithWarningResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                emptyList(),
                emptyMap(),
                result.fieldUidWarningList,
                eventMode,
            )

            is MissingMandatoryResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                result.errorFields,
                result.mandatoryFields,
                result.warningFields,
                eventMode,
            )

            is SuccessfulResult -> activityPresenter.attemptFinish(
                result.canComplete,
                result.onCompleteMessage,
                emptyList(),
                emptyMap(),
                emptyList(),
            )

            NotSavedResult -> {
                // Nothing to do in this case
            }
        }
    }

    fun onFieldsLoading(fields: List<FieldUiModel>): List<FieldUiModel> {
        val updatedFields = updateBiometricsField(fields)

        if (BIOMETRICS_ENABLED) {

            biometricsVerificationUiModel = updatedFields.firstOrNull {
                it is BiometricsDataElementUiModelImpl
            }?.let { it as BiometricsDataElementUiModelImpl }


            biometricsVerificationUiModel?.setBiometricsRetryListener(
                object : BiometricsDataElementUiModelImpl.BiometricsReTryOnClickListener {
                    override fun onRetryClick() {
                        val ageInMonths = activityPresenter.getTEIAgeInMonths()

                        val orgUnitAsModuleId = getOrgUnitAsModuleId(teiOrgUnit ?: "", d2, basicPreferenceProvider)

                        view.verifyBiometrics(biometricsGuid, orgUnitAsModuleId, trackedEntityInstanceId, ageInMonths)
                    }
                }
            )

            biometricsVerificationUiModel?.setBiometricsRegisterListener(
                object : BiometricsDataElementUiModelImpl.BiometricsRegisterClickListener {
                    override fun onClick() {
                        val ageInMonths = activityPresenter.getTEIAgeInMonths()

                        val orgUnitAsModuleId = getOrgUnitAsModuleId(teiOrgUnit?:"", d2, basicPreferenceProvider)

                        view.registerBiometrics(orgUnitAsModuleId, trackedEntityInstanceId, ageInMonths)
                    }
                }
            )

            if (biometricsVerificationUiModel != null) {
                launchBiometricsVerificationIfRequired(updatedFields)
            }
        }

        return updatedFields
    }

    private fun updateBiometricsField(fields: List<FieldUiModel>?): MutableList<FieldUiModel> {
        val biometricsMode = getBiometricsConfig(basicPreferenceProvider).biometricsMode

        val finalFields = if (biometricsMode == BiometricsMode.full) fields else fields?.filter {
            it !is BiometricsDataElementUiModelImpl
        }?.toMutableList()

        return finalFields?.map {
            if (it is BiometricsDataElementUiModelImpl) {
                val biometricsUiModel = it as BiometricsDataElementUiModelImpl
                val status = mapVerificationStatus(biometricsVerificationStatus)
                biometricsUiModel
                    .setValue(biometricsGuid)
                    .setStatus(status)
            } else {
                it
            }
        } as MutableList<FieldUiModel>
    }

    private fun launchBiometricsVerificationIfRequired(fields: List<FieldUiModel>) {
        biometricsVerificationUiModel = fields.firstOrNull {
            it is BiometricsDataElementUiModelImpl
        }?.let { it as BiometricsDataElementUiModelImpl }

        if (biometricsVerificationUiModel != null && this.biometricsGuid != null &&
            this.biometricsGuid!!.isNotBlank() &&
            biometricsVerificationUiModel!!.status == BiometricsDataElementStatus.NOT_DONE
        ) {
            val lastUpdated = activityPresenter.getBiometricsAttributeValueInTeiLastUpdated(
                biometricsVerificationUiModel?.uid
            )

            if (!isLastVerificationValid(
                    lastUpdated,
                    activityPresenter.getLastBiometricsVerificationDuration(),
                    true
                )
            ) {
                val ageInMonths = activityPresenter.getTEIAgeInMonths()

                val orgUnitAsModuleId = getOrgUnitAsModuleId(this.teiOrgUnit ?: "", d2, basicPreferenceProvider)

                view.verifyBiometrics(
                    this.biometricsGuid,
                    orgUnitAsModuleId,
                    this.trackedEntityInstanceId,
                    ageInMonths
                )
            } else {
                refreshBiometricsStatus(1, false)
            }
        }
    }

    fun showOrHideSaveButton() {
        val isEditable =
            d2.eventModule().eventService().getEditableStatus(eventUid = eventUid).blockingGet()

        when (isEditable) {
            is EventEditableStatus.Editable -> {
                view.showSaveButton()
            }

            is EventEditableStatus.NonEditable -> {
                view.hideSaveButton()
                configureNonEditableMessage(isEditable.reason)
            }
        }
    }

    private fun configureNonEditableMessage(eventNonEditableReason: EventNonEditableReason) {
        val (reason, canBeReOpened) = when (eventNonEditableReason) {
            EventNonEditableReason.BLOCKED_BY_COMPLETION -> resourceManager.getString(R.string.blocked_by_completion) to canReopen()
            EventNonEditableReason.EXPIRED -> resourceManager.getString(R.string.edition_expired) to false
            EventNonEditableReason.NO_DATA_WRITE_ACCESS -> resourceManager.getString(R.string.edition_no_write_access) to false
            EventNonEditableReason.EVENT_DATE_IS_NOT_IN_ORGUNIT_RANGE -> resourceManager.getString(R.string.event_date_not_in_orgunit_range) to false
            EventNonEditableReason.NO_CATEGORY_COMBO_ACCESS -> resourceManager.getString(R.string.edition_no_catcombo_access) to false
            EventNonEditableReason.ENROLLMENT_IS_NOT_OPEN -> resourceManager.formatWithEnrollmentLabel(
                d2.eventModule().events().uid(eventUid).blockingGet()?.program(),
                R.string.edition_enrollment_is_no_open_V2,
                1,
            ) to false

            EventNonEditableReason.ORGUNIT_IS_NOT_IN_CAPTURE_SCOPE -> resourceManager.getString(R.string.edition_orgunit_capture_scope) to false
        }
        view.showNonEditableMessage(reason, canBeReOpened)
    }

    fun reOpenEvent() {
        EventIdlingResourceSingleton.increment()
        CoroutineScope(dispatcherProvider.ui()).launch {
            reOpenEventUseCase(eventUid).fold(
                onSuccess = {
                    view.onReopen()
                    view.showSaveButton()
                    view.hideNonEditableMessage()
                    EventIdlingResourceSingleton.decrement()
                },
                onFailure = { error ->
                    resourceManager.parseD2Error(error)
                    EventIdlingResourceSingleton.decrement()
                },
            )
        }
    }

    private fun canReopen(): Boolean = getEvent()?.let {
        it.status() == EventStatus.COMPLETED && hasReopenAuthority()
    } ?: false

    private fun getEvent(): Event? {
        return d2.eventModule().events().uid(eventUid).blockingGet()
    }

    private fun hasReopenAuthority(): Boolean = d2.userModule().authorities()
        .byName().`in`(AUTH_UNCOMPLETE_EVENT, AUTH_ALL)
        .one()
        .blockingExists()

    // EyeSeeTea customizations

    fun initBiometricsValues(
        biometricsGuid: @Nullable String?,
        biometricsVerificationStatus: Int,
        teiOrgUnit: @Nullable String?,
        trackedEntityInstanceId: @Nullable String?
    ) {
        this.biometricsGuid = biometricsGuid
        this.biometricsVerificationStatus = biometricsVerificationStatus
        this.teiOrgUnit = teiOrgUnit
        this.trackedEntityInstanceId = trackedEntityInstanceId
    }

    fun refreshBiometricsStatus(
        biometricsVerificationStatus: Int,
        updateBiometricsGuidInAttribute: Boolean = true,
        newBiometricsGuid: String? = null
    ) {
        if (newBiometricsGuid != null) {
            biometricsGuid = newBiometricsGuid
        }

        val status = mapVerificationStatus(biometricsVerificationStatus)

        if (status == BiometricsDataElementStatus.SUCCESS && updateBiometricsGuidInAttribute) {
            activityPresenter.updateBiometricsAttributeValueInTei(biometricsGuid)
        }

        this.biometricsVerificationStatus = biometricsVerificationStatus

        view.onReopen()
    }

    private fun mapVerificationStatus(biometricsVerificationStatus: Int): BiometricsDataElementStatus {
        return when (biometricsVerificationStatus) {
            0 -> BiometricsDataElementStatus.FAILURE
            1 -> BiometricsDataElementStatus.SUCCESS
            else -> BiometricsDataElementStatus.NOT_DONE
        }
    }
}
