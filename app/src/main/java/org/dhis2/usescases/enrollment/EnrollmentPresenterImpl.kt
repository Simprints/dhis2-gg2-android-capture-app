package org.dhis2.usescases.enrollment

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.bindings.trackedEntityTypeForTei
import org.dhis2.commons.biometrics.BIOMETRICS_FAILURE_PATTERN
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.data.TeiAttributesInfo
import org.dhis2.commons.matomo.Actions.Companion.CREATE_TEI
import org.dhis2.commons.matomo.Categories.Companion.TRACKER_LIST
import org.dhis2.commons.matomo.Labels.Companion.CLICK
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.schedulers.defaultSubscribe
import org.dhis2.data.biometrics.SimprintsItem
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.data.biometrics.utils.getBiometricsTrackedEntityAttribute
import org.dhis2.data.biometrics.utils.getTeiByUid
import org.dhis2.data.biometrics.utils.getTrackedEntityAttributeValueByAttribute
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.biometrics.BiometricsAttributeUiModelImpl
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.duplicates.LastPossibleDuplicates
import org.dhis2.usescases.biometrics.entities.BiometricsMode
import org.dhis2.usescases.biometrics.getAgeInMonthsByFieldUiModel
import org.dhis2.usescases.biometrics.getOrgUnitAsModuleId
import org.dhis2.usescases.biometrics.isUnderAgeThreshold
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

private const val TAG = "EnrollmentPresenter"

class EnrollmentPresenterImpl(
    val view: EnrollmentView,
    val d2: D2,
    private val enrollmentObjectRepository: EnrollmentObjectRepository,
    private val dataEntryRepository: EnrollmentRepository,
    private val teiRepository: TrackedEntityInstanceObjectRepository,
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program>,
    private val schedulerProvider: SchedulerProvider,
    private val enrollmentFormRepository: EnrollmentFormRepository,
    private val analyticsHelper: AnalyticsHelper,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val eventCollectionRepository: EventCollectionRepository,
    private val teiAttributesProvider: TeiAttributesProvider,
    private val basicPreferenceProvider: BasicPreferenceProvider,
) {

    private var pendingSave: Boolean = false
    private val disposable = CompositeDisposable()
    private val backButtonProcessor: FlowableProcessor<Boolean> = PublishProcessor.create()
    private var hasShownIncidentDateEditionWarning = false
    private var hasShownEnrollmentDateEditionWarning = false
    private var biometricsUiModel: BiometricsAttributeUiModelImpl? = null
    private val lastDeclinedEnrolDuration = basicPreferenceProvider.getInt(
        BiometricsPreference.LAST_DECLINED_ENROL_DURATION, 0
    )
    private var resetBiometricsFailureAfterTimeDisposable: Disposable? = null
    private var lastPossibleDuplicates: LastPossibleDuplicates? = null

    val biometricsMode = getBiometricsConfig(basicPreferenceProvider).biometricsMode

    fun init() {
        view.setSaveButtonVisible(false)

        disposable.add(
            teiRepository.get()
                .map { tei ->
                    val attrList = mutableListOf<String>()
                    val attributesValues =
                        teiAttributesProvider
                            .getListOfValuesFromProgramTrackedEntityAttributesByProgram(
                                programRepository.blockingGet()?.uid() ?: "",
                                tei.uid(),
                            )
                    val teiTypeAttributeValue = mutableListOf<TrackedEntityAttributeValue>()
                    if (attributesValues.isEmpty()) {
                        teiTypeAttributeValue.addAll(
                            teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(
                                tei.trackedEntityType(),
                                tei.uid(),
                            ),
                        )
                        attrList.addAll(teiTypeAttributeValue.map { it.value() ?: "" })
                    } else {
                        attrList.addAll(attributesValues.map { it.value() ?: "" })
                    }

                    TeiAttributesInfo(
                        attributes = attrList,
                        profileImage = tei.profilePicturePath(
                            d2,
                            programRepository.blockingGet()?.uid(),
                        ),
                        teTypeName = d2.trackedEntityTypeForTei(tei.uid())?.displayName()!!,
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::displayTeiInfo,
                    Timber.tag(TAG)::e,
                ),
        )

        disposable.add(
            programRepository.get()
                .map { it.access()?.data()?.write() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.setAccess(it) },
                    { Timber.tag(TAG).e(it) },
                ),
        )

        disposable.add(
            enrollmentObjectRepository.get()
                .map { it.status() }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.renderStatus(it!!) },
                    { Timber.tag(TAG).e(it) },
                ),
        )
    }

    private fun shouldShowDateEditionWarning(uid: String): Boolean {
        return if (uid == EnrollmentRepository.ENROLLMENT_DATE_UID &&
            dataEntryRepository.hasEventsGeneratedByEnrollmentDate() &&
            !hasShownEnrollmentDateEditionWarning
        ) {
            hasShownEnrollmentDateEditionWarning = true
            true
        } else if (uid == EnrollmentRepository.INCIDENT_DATE_UID &&
            dataEntryRepository.hasEventsGeneratedByIncidentDate() &&
            !hasShownIncidentDateEditionWarning
        ) {
            hasShownIncidentDateEditionWarning = true
            true
        } else {
            false
        }
    }

    fun subscribeToBackButton() {
        disposable.add(
            backButtonProcessor
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.performSaveClick() },
                    { t -> Timber.e(t) },
                ),
        )
    }

    fun finish(enrollmentMode: EnrollmentActivity.EnrollmentMode) {
        when (enrollmentMode) {
            EnrollmentActivity.EnrollmentMode.NEW -> {
                matomoAnalyticsController.trackEvent(TRACKER_LIST, CREATE_TEI, CLICK)
                disposable.add(
                    enrollmentFormRepository.generateEvents()
                        .defaultSubscribe(
                            schedulerProvider,
                            {
                                it.second?.let { eventUid ->
                                    view.openEvent(eventUid)
                                } ?: view.openDashboard(it.first)
                            },
                            { Timber.tag(TAG).e(it) },
                        ),
                )
            }

            EnrollmentActivity.EnrollmentMode.CHECK -> view.setResultAndFinish()
        }
    }

    fun updateFields(action: RowAction? = null) {
        action?.let {
            if (shouldShowDateEditionWarning(it.id)) {
                view.showDateEditionWarning()
            }
        }
    }

    fun backIsClicked() {
        backButtonProcessor.onNext(true)
    }

    fun getEnrollment(): Enrollment? {
        return enrollmentObjectRepository.blockingGet()
    }

    fun getProgram(): Program? {
        return programRepository.blockingGet()
    }

    fun updateEnrollmentStatus(newStatus: EnrollmentStatus): Boolean {
        return try {
            if (getProgram()?.access()?.data()?.write() == true) {
                enrollmentObjectRepository.setStatus(newStatus)
                view.renderStatus(newStatus)
                true
            } else {
                view.displayMessage(null)
                false
            }
        } catch (error: D2Error) {
            false
        }
    }

    fun saveEnrollmentGeometry(geometry: Geometry?) {
        enrollmentObjectRepository.setGeometry(geometry)
    }

    fun saveTeiGeometry(geometry: Geometry?) {
        teiRepository.setGeometry(geometry)
    }

    fun deleteAllSavedData() {
        val isTeiInNoOtherProgram by lazy {
            dataEntryRepository.isTeiInNoOtherProgram(
                teiUid = teiRepository.blockingGet()?.uid(),
                programUid = programRepository.blockingGet()?.uid(),
            )
        }
        if (teiRepository.blockingGet()?.syncState() == State.TO_POST && isTeiInNoOtherProgram) {
            teiRepository.blockingDelete()
        } else {
            enrollmentObjectRepository.blockingDelete()
        }
        analyticsHelper.setEvent(DELETE_AND_BACK, CLICK, DELETE_AND_BACK)
    }

    fun onDettach() {
        disposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun onTeiImageHeaderClick() {
        val picturePath = enrollmentFormRepository.getProfilePicture()
        if (picturePath.isNotEmpty()) {
            view.displayTeiPicture(picturePath)
        }
    }

    private fun isBiometricsAvailable(): Boolean =
        BIOMETRICS_ENABLED && biometricsUiModel != null

    fun showOrHideSaveButton() {
        val teiUid = teiRepository.blockingGet()?.uid() ?: ""
        val programUid = getProgram()?.uid() ?: ""
        val hasEnrollmentAccess = d2.enrollmentModule().enrollmentService()
            .blockingGetEnrollmentAccess(teiUid, programUid)
        if (!isBiometricsAvailable() && hasEnrollmentAccess == EnrollmentAccess.WRITE_ACCESS) {
            view.setSaveButtonVisible(visible = true)
        } else {
            view.setSaveButtonVisible(visible = false)
        }
    }

    fun isEventScheduleOrSkipped(eventUid: String): Boolean {
        val event = eventCollectionRepository.uid(eventUid).blockingGet()
        return event?.status() == EventStatus.SCHEDULE ||
                event?.status() == EventStatus.SKIPPED ||
                event?.status() == EventStatus.OVERDUE
    }

    fun onBiometricsCompleted(guid: String) {
        lastPossibleDuplicates = null
        saveBiometricValue(guid)
    }

    fun onBiometricsFailure() {
        pendingSave = false

        val uuid: UUID = UUID.randomUUID()
        saveBiometricValue("${BIOMETRICS_FAILURE_PATTERN}_${uuid}")
    }

    fun checkIfBiometricValueValid() {
        if (BIOMETRICS_ENABLED) {
            if (biometricsUiModel != null && biometricsUiModel!!.value != null && biometricsUiModel!!.value!!.startsWith(
                    BIOMETRICS_FAILURE_PATTERN
                )
            ) {
                saveBiometricValue(null)
            }
        }
    }

    private fun saveBiometricValue(value: String?) {
        biometricsUiModel!!.onTextChange(value)
        biometricsUiModel!!.onSave(value)

        if (pendingSave) {
            pendingSave = false

            runBlocking {
                delay(200)
                view.performSaveClick()
            }

            return
        }
    }

    fun onBiometricsPossibleDuplicates(
        possibleDuplicates: List<SimprintsItem>,
        sessionId: String,
        enrollNewVisible: Boolean = true
    ) {
        val program = getProgram()!!.uid()
        val biometricsAttUid = biometricsUiModel!!.uid
        val teiUid = getEnrollment()!!.trackedEntityInstance()

        val teiTypeUid = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            ?.trackedEntityType()!!

        if (possibleDuplicates.isEmpty()) {
            view.registerLast(sessionId)
        } else if (possibleDuplicates.size == 1 && possibleDuplicates[0].guid == biometricsUiModel!!.value) {
            view.registerLast(sessionId)
        } else {
            val finalPossibleDuplicates =
                possibleDuplicates.filter { it.guid != biometricsUiModel!!.value }

            lastPossibleDuplicates = LastPossibleDuplicates(finalPossibleDuplicates, sessionId)

            view.hideProgress()
            view.showPossibleDuplicatesDialog(
                finalPossibleDuplicates,
                sessionId,
                program,
                teiTypeUid,
                biometricsAttUid,
                enrollNewVisible
            )
        }
    }

    fun onFieldsLoaded(fields: List<FieldUiModel>) {
        if (BIOMETRICS_ENABLED) {
            biometricsUiModel = fields.firstOrNull {
                it is BiometricsAttributeUiModelImpl
            }?.let { it as BiometricsAttributeUiModelImpl }

            biometricsUiModel?.let {
                showOrHideSaveButton()
            }

            biometricsUiModel?.setBiometricsRegisterListener {
                val orgUnit = enrollmentObjectRepository.get().blockingGet()
                    ?.organisationUnit()!!

                val ageInMonths =
                    getAgeInMonthsByFieldUiModel(basicPreferenceProvider, fields)

                val orgUnitAsModuleId = getOrgUnitAsModuleId(orgUnit, d2, basicPreferenceProvider)

                view.registerBiometrics(orgUnitAsModuleId, ageInMonths)
                pendingSave = true
            }

            biometricsUiModel?.setSaveTEI { removeBiometrics ->
                if (removeBiometrics) {
                    saveBiometricValue(null)
                }

                view.performSaveClick()
            }

            biometricsUiModel?.setRegisterLastAndSave { sessionId ->
                pendingSave = true
                view.registerLast(sessionId)
            }

            if (biometricsUiModel?.value?.startsWith(BIOMETRICS_FAILURE_PATTERN) == true) {
                resetBiometricsFailureAfterTime()
            }
        }
    }

    private fun resetBiometricsFailureAfterTime() {
        if (resetBiometricsFailureAfterTimeDisposable != null &&
            !resetBiometricsFailureAfterTimeDisposable!!.isDisposed
        ) {
            resetBiometricsFailureAfterTimeDisposable!!.dispose()
        }

        resetBiometricsFailureAfterTimeDisposable =
            (Observable.timer(lastDeclinedEnrolDuration.toLong(), TimeUnit.MINUTES)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    {
                        saveBiometricValue(null)
                    }
                ) { t: Throwable? -> Timber.e(t) })

        disposable.add(resetBiometricsFailureAfterTimeDisposable!!)
    }

    fun onFieldsLoading(fields: List<FieldUiModel>): List<FieldUiModel> {

        val finalFields = if (biometricsMode == BiometricsMode.full) fields else fields.filter {
            it !is BiometricsAttributeUiModelImpl
        }.toMutableList()

        val allMandatoryFieldsHasValue =
            finalFields.count { it.mandatory && (it.value == null || it.value!!.isEmpty()) } == 0

        return finalFields.map {
            if (it is BiometricsAttributeUiModelImpl) {
                val biometricsUiModel = it

                val teiUid = teiRepository.blockingGet()?.uid() ?: return fields

                val tei = getTeiByUid(d2, teiUid)

                val teiAttrValues = tei?.trackedEntityAttributeValues() ?: listOf()

                val isUnderAgeThreshold =
                    isUnderAgeThreshold(basicPreferenceProvider, teiAttrValues)

                biometricsUiModel
                    .setValue(biometricsUiModel.value)
                    .setEditable(allMandatoryFieldsHasValue)
                    .setAgeUnderThreshold(isUnderAgeThreshold)
            } else {
                it
            }
        } as MutableList<FieldUiModel>
    }

    fun getBiometricsGuid(): String? {
        val teiUid = teiRepository.blockingGet()?.uid() ?: return null

        val teiValues = getTeiByUid(d2, teiUid)?.trackedEntityAttributeValues() ?: return null

        val biometricsAttribute = getBiometricsTrackedEntityAttribute(d2) ?: return null

        val attValue = getTrackedEntityAttributeValueByAttribute(
            biometricsAttribute,
            teiValues
        )

        return attValue?.value()
    }
}
