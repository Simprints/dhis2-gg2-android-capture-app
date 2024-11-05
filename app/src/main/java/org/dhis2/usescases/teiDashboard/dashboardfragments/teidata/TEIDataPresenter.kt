package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.dhis2.commons.Constants
import org.dhis2.commons.bindings.canCreateEventInEnrollment
import org.dhis2.commons.bindings.enrollment
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.EventViewModelType
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometrics.RegisterResult
import org.dhis2.data.biometrics.SimprintsItem
import org.dhis2.data.biometrics.VerifyResult
import org.dhis2.data.biometrics.getBiometricsConfig
import org.dhis2.form.data.FormValueStore
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.model.EventMode
import org.dhis2.mobileProgramRules.RuleEngineHelper
import org.dhis2.usescases.biometrics.biometricAttributeId
import org.dhis2.usescases.biometrics.entities.BiometricsMode
import org.dhis2.usescases.biometrics.duplicates.LastPossibleDuplicates
import org.dhis2.usescases.biometrics.getAgeInMonthsByAttributes
import org.dhis2.usescases.biometrics.getOrgUnitAsModuleId
import org.dhis2.usescases.biometrics.isLastVerificationValid
import org.dhis2.usescases.biometrics.isUnderAgeThreshold
import org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics.TeiDashboardBioModel
import org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics.TeiDashboardBioRegistrationMapper
import org.dhis2.usescases.biometrics.ui.teiDashboardBiometrics.TeiDashboardBioVerificationMapper
import org.dhis2.usescases.events.ScheduledEventActivity.Companion.getIntent
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity.Companion.getActivityBundleWithBiometrics
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.programEventDetail.usecase.CreateEventUseCase
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity
import org.dhis2.usescases.teiDashboard.DashboardEnrollmentModel
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.decrement
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TeiDataIdlingResourceSingleton.increment
import org.dhis2.usescases.teiDashboard.domain.GetNewEventCreationTypeOptions
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.dhis2.utils.Result
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CREATE_EVENT_TEI
import org.dhis2.utils.analytics.TYPE_EVENT_TEI
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber
import java.util.Timer
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class TEIDataPresenter(
    private val view: TEIDataContracts.View,
    private val d2: D2,
    private val dashboardRepository: DashboardRepository,
    private val teiDataRepository: TeiDataRepository,
    private val ruleEngineHelper: RuleEngineHelper?,
    private var programUid: String?,
    private val teiUid: String,
    private val enrollmentUid: String,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val valueStore: FormValueStore,
    private val optionsRepository: OptionsRepository,
    private val getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions,
    private val eventCreationOptionsMapper: EventCreationOptionsMapper,
    private val contractHandler: TeiDataContractHandler,
    private val dispatcher: DispatcherProvider,
    private val createEventUseCase: CreateEventUseCase,
    private val d2ErrorUtils: D2ErrorUtils,
    private val basicPreferenceProvider: BasicPreferenceProvider,
    private val resourceManager: ResourceManager,
    private val lastBiometricsSearchSessionId: String?
) {
    private var dashboardModel: DashboardEnrollmentModel? = null
    private val groupingProcessor: BehaviorProcessor<Boolean> = BehaviorProcessor.create()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var currentStage: String = ""
    private var stagesToHide: List<String> = emptyList()

    private val _shouldDisplayEventCreationButton = MutableLiveData(false)
    val shouldDisplayEventCreationButton: LiveData<Boolean> = _shouldDisplayEventCreationButton

    private val _events: MutableLiveData<List<EventViewModel>> = MutableLiveData()
    val events: LiveData<List<EventViewModel>> = _events

    private var uidForEvent: String? = null
    private var orgUnitUid: String? = null

    private var lastVerificationResult: VerifyResult? = null
    private var lastRegisterResult: RegisterResult? = null
    private val lastBiometricsVerificationDuration = basicPreferenceProvider.getInt(
        BiometricsPreference.LAST_VERIFICATION_DURATION, 0
    )
    private val lastDeclinedEnrolDuration = basicPreferenceProvider.getInt(
        BiometricsPreference.LAST_DECLINED_ENROL_DURATION, 0
    )

    private var lastPossibleDuplicates: LastPossibleDuplicates? = null

    private val biometricsMode = getBiometricsConfig(basicPreferenceProvider).biometricsMode

    fun init() {
        programUid?.let {
            val program = d2.program(it) ?: throw NullPointerException()
            val sectionFlowable = view.observeStageSelection(program)
                .startWith(StageSection("", false, false))
                .map { (stageUid, showOptions, showAllEvents) ->
                    currentStage = if (stageUid == currentStage && !showOptions) "" else stageUid
                    StageSection(currentStage, showOptions, showAllEvents)
                }
            val programHasGrouping = dashboardRepository.getGrouping()
            val groupingFlowable = groupingProcessor.startWith(programHasGrouping)

            compositeDisposable.add(
                Flowable.combineLatest<StageSection?, Boolean?, Pair<StageSection, Boolean>>(
                    sectionFlowable,
                    groupingFlowable,
                    ::Pair,
                )
                    .doOnNext { increment() }
                    .switchMap { stageAndGrouping ->
                        Flowable.zip(
                            teiDataRepository.getTEIEnrollmentEvents(
                                stageAndGrouping.first,
                                stageAndGrouping.second,
                            ).toFlowable(),
                            Flowable.fromCallable {
                                ruleEngineHelper?.refreshContext()
                                (ruleEngineHelper?.evaluate() ?: emptyList())
                                    .let { ruleEffects -> Result.success(ruleEffects) }
                            },
                        ) { events, calcResult ->
                            applyEffects(
                                events,
                                calcResult,
                            )
                        }.subscribeOn(schedulerProvider.io())
                    }
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { events ->
                            _events.postValue(events)
                            decrement()
                        },
                        Timber.Forest::d,
                    ),
            )

            fetchDashboardModel()

            getEventsWithoutCatCombo()

            compositeDisposable.add(
                Observable.interval(0, 2, TimeUnit.SECONDS)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { refreshVerificationStatus() }
                    ) { t: Throwable? -> Timber.e(t) })

        } ?: run {
            _shouldDisplayEventCreationButton.value = false
        }

        updateCreateEventButtonVisibility()
    }

    private fun fetchDashboardModel() {
        compositeDisposable.add(
            Observable.just(dashboardRepository.getDashboardModel())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { model ->
                        if (model is DashboardEnrollmentModel) {
                            dashboardModel = model
                            programUid = model.currentProgram().uid()
                            orgUnitUid = model.orgUnits[0].uid()

                            refreshVerificationStatus()
                        }
                    },
                    Timber.Forest::e,
                ),
        )
        dashboardRepository.getDashboardModel()
    }


    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateCreateEventButtonVisibility() {
        CoroutineScope(dispatcher.io()).launch {
            val isGrouping = dashboardRepository.getGrouping()
            val enrollment = d2.enrollment(enrollmentUid)
            val showButton =
                enrollment != null &&
                        !isGrouping && enrollment.status() == EnrollmentStatus.ACTIVE &&
                        canAddNewEvents()
            _shouldDisplayEventCreationButton.postValue(showButton)
        }
    }

    private fun applyEffects(
        events: List<EventViewModel>,
        calcResult: Result<RuleEffect>,
    ): List<EventViewModel> {
        Timber.d("APPLYING EFFECTS")
        if (calcResult.error() != null) {
            Timber.e(calcResult.error())
            view.showProgramRuleErrorMessage()
            return emptyList()
        }
        val (_, _, _, _, _, _, stagesToHide1) = RulesUtilsProviderImpl(
            d2,
            optionsRepository,
        ).applyRuleEffects(
            false,
            HashMap(),
            calcResult.items(),
            valueStore,
        )
        stagesToHide = stagesToHide1
        return events.mapNotNull {
            it.applyHideStage(stagesToHide.contains(it.stage?.uid()))
        }
    }

    @VisibleForTesting
    fun getEventsWithoutCatCombo() {
        compositeDisposable.add(
            teiDataRepository.eventsWithoutCatCombo()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::displayCatComboOptionSelectorForEvents,
                    Timber.Forest::e,
                ),
        )
    }

    fun changeCatOption(eventUid: String?, catOptionComboUid: String?) {
        dashboardRepository.saveCatOption(eventUid, catOptionComboUid)
    }

    fun areEventsCompleted() {
        compositeDisposable.add(
            dashboardRepository.getEnrollmentEventsWithDisplay(programUid, teiUid)
                .flatMap { events ->
                    if (events.isEmpty()) {
                        dashboardRepository.getTEIEnrollmentEvents(
                            programUid,
                            teiUid,
                        )
                    } else {
                        Observable.just(events)
                    }
                }
                .map { events ->
                    Observable.fromIterable(events)
                        .all { event -> event.status() == EventStatus.COMPLETED }
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view.areEventsCompleted(),
                    Timber.Forest::d,
                ),
        )
    }

    fun displayGenerateEvent(eventUid: String?) {
        compositeDisposable.add(
            dashboardRepository.displayGenerateEvent(eventUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe({ programStage ->
                    if (programStage.displayGenerateEventBox() == true || programStage.allowGenerateNextVisit() == true) {
                        view.displayScheduleEvent()
                    } else if (programStage.remindCompleted() == true) {
                        view.showDialogCloseProgram()
                    }
                }, Timber.Forest::d),
        )
    }

    fun completeEnrollment() {
        val hasWriteAccessInProgram =
            programUid?.let { d2.program(it)?.access()?.data()?.write() } == true

        if (hasWriteAccessInProgram) {
            compositeDisposable.add(
                Completable.fromCallable {
                    dashboardRepository.completeEnrollment(enrollmentUid).blockingFirst()
                }
                    .subscribeOn(schedulerProvider.computation())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        {},
                        Timber.Forest::d,
                    ),
            )
        } else {
            view.displayMessage(null)
        }
    }

    fun onEventCreationClick(eventCreationId: Int) {
        createEventInEnrollment(eventCreationOptionsMapper.getActionType(eventCreationId))
    }

    private fun createEventInEnrollment(
        eventCreationType: EventCreationType,
        scheduleIntervalDays: Int = 0,
    ) {
        analyticsHelper.setEvent(TYPE_EVENT_TEI, eventCreationType.name, CREATE_EVENT_TEI)
        val bundle = Bundle()
        bundle.putString(
            Constants.PROGRAM_UID,
            programUid,
        )
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid)
        teiDataRepository.getEnrollment().blockingGet()?.organisationUnit()
            ?.takeIf { enrollmentOrgUnitInCaptureScope(it) }?.let {
                bundle.putString(Constants.ORG_UNIT, it)
            }

        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid)
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType.name)
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays)
        val intent = Intent(view.context, ProgramStageSelectionActivity::class.java)
        intent.putExtras(bundle)
        contractHandler.createEvent(intent).observe(view.viewLifecycleOwner()) {
            fetchEvents()
        }
    }

    fun onScheduleSelected(uid: String?, sharedView: View?) {
        uid?.let {
            val intent =
                getIntent(view.context, uid, dashboardModel!!.getBiometricValue(), -1, orgUnitUid!!)
            val options = sharedView?.let { it1 ->
                ActivityOptionsCompat.makeSceneTransitionAnimation(
                    view.abstractActivity,
                    it1,
                    "shared_view",
                )
            } ?: ActivityOptionsCompat.makeBasic()
            view.openEventDetails(intent, options)
        }
    }

    fun onEventSelected(uid: String, eventStatus: EventStatus) {
        lastVerificationResult = null

        if (eventStatus == EventStatus.ACTIVE || eventStatus == EventStatus.COMPLETED) {
            uidForEvent = uid

            launchEventCapture(uid, dashboardModel?.getBiometricValue() ?: "", -1)
        } else {
            val event = d2.event(uid)
            val intent = Intent(view.context, EventInitialActivity::class.java)
            intent.putExtras(
                EventInitialActivity.getBundleWithBiometrics(
                    programUid,
                    uid,
                    EventCreationType.DEFAULT.name,
                    teiUid,
                    null,
                    event?.organisationUnit(),
                    event?.programStage(),
                    enrollmentUid,
                    0,
                    teiDataRepository.getEnrollment().blockingGet()?.status(),
                    dashboardModel?.getBiometricValue() ?: "",
                    -1,
                    orgUnitUid
                )
            )
            view.openEventInitial(intent)
        }
    }

    fun setProgram(program: Program, enrollmentUid: String?) {
        program.uid()?.let { uid ->
            programUid = uid
            enrollmentUid?.let { view.restoreAdapter(uid, teiUid, it) }
        }
    }

    fun onDettach() {
        compositeDisposable.clear()
    }

    fun displayMessage(message: String?) {
        view.displayMessage(message)
    }

    fun showDescription(description: String?) {
        view.showDescription(description)
    }

    fun onGroupingChanged(shouldGroupBool: Boolean) {
        programUid?.let {
            groupingProcessor.onNext(shouldGroupBool)
            updateCreateEventButtonVisibility()
        }
    }

    fun onSyncDialogClick(eventUid: String) {
        view.showSyncDialog(eventUid, enrollmentUid)
    }

    fun enrollmentOrgUnitInCaptureScope(enrollmentOrgUnit: String) =
        teiDataRepository.enrollmentOrgUnitInCaptureScope(enrollmentOrgUnit)

    private fun canAddNewEvents(): Boolean {
        return d2.canCreateEventInEnrollment(enrollmentUid, stagesToHide)
    }

    fun getOrgUnitName(orgUnitUid: String): String {
        return teiDataRepository.getOrgUnitName(orgUnitUid)
    }

    fun onAddNewEventOptionSelected(eventCreationType: EventCreationType, stage: ProgramStage?) {
        if (stage != null) {
            when (eventCreationType) {
                EventCreationType.ADDNEW -> programUid?.let { program ->
                    checkOrgUnitCount(program, stage.uid())
                }

                else -> view.goToEventInitial(eventCreationType, stage)
            }
        } else {
            createEventInEnrollment(eventCreationType)
        }
    }

    fun getNewEventOptionsByStages(stage: ProgramStage?): List<EventCreationOptions> {
        val options = programUid?.let { getNewEventCreationTypeOptions(stage, it) }
        return options?.let { eventCreationOptionsMapper.mapToEventsByStage(it) } ?: emptyList()
    }

    fun fetchEvents() {
        groupingProcessor.onNext(dashboardRepository.getGrouping())
    }

    fun getEnrollment(): Enrollment? {
        return teiDataRepository.getEnrollment().blockingGet()
    }

    fun filterAvailableStages(programStages: List<ProgramStage>): List<ProgramStage> =
        programStages
            .filter { it.access().data().write() }
            .filter { !stagesToHide.contains(it.uid()) }
            .filter { stage ->
                stage.repeatable() == true ||
                        events.value?.none { event ->
                            event.stage?.uid() == stage.uid() &&
                                    event.type == EventViewModelType.EVENT
                        } == true
            }.sortedBy { stage -> stage.sortOrder() }

    fun checkOrgUnitCount(programUid: String, programStageUid: String) {
        CoroutineScope(dispatcher.io()).launch {
            val orgUnits = teiDataRepository.programOrgListInCaptureScope(programUid)
            if (orgUnits.count() == 1) {
                onOrgUnitForNewEventSelected(orgUnits.first().uid(), programStageUid)
            } else {
                view.displayOrgUnitSelectorForNewEvent(programUid, programStageUid)
            }
        }
    }

    fun onOrgUnitForNewEventSelected(orgUnitUid: String, programStageUid: String) {
        CoroutineScope(dispatcher.io()).launch {
            programUid?.let {
                createEventUseCase(
                    programUid = it,
                    orgUnitUid = orgUnitUid,
                    programStageUid = programStageUid,
                    enrollmentUid = enrollmentUid,
                ).fold(
                    onSuccess = { eventUid ->
                        view.goToEventDetails(
                            eventUid = eventUid,
                            eventMode = EventMode.NEW,
                            programUid = it,
                        )
                    },
                    onFailure = { d2Error ->
                        view.displayMessage(d2ErrorUtils.getErrorMessage(d2Error))
                    },
                )
            }
        }
    }

    fun launchEventCapture(uid: String, guid: String, status: Int) {
        val finalIid = uidForEvent ?: uid

        val intent = Intent(view.context, EventCaptureActivity::class.java)
        intent.putExtras(
            getActivityBundleWithBiometrics(
                eventUid = finalIid,
                programUid = programUid ?: throw IllegalStateException(),
                eventMode = EventMode.CHECK,
                guid = guid,
                status = status,
                orgUnitUid = orgUnitUid!!,
                trackedEntityInstanceId = teiUid
            )
        )
        view.openEventCapture(intent)
    }

    private fun verifyBiometrics() {
        if (dashboardModel != null) {
            val biometricValue = dashboardModel!!.getBiometricValue() ?: return
            val orgUnit = orgUnitUid ?: return

            val ageInMonths = getAgeInMonthsByAttributes(
                basicPreferenceProvider,
                dashboardModel!!.trackedEntityAttributeValues
            )

            val orgUnitAsModuleId = getOrgUnitAsModuleId(orgUnit, d2, basicPreferenceProvider)

            view.launchBiometricsVerification(
                biometricValue,
                orgUnitAsModuleId, dashboardModel!!.trackedEntityInstance.uid(),
                ageInMonths
            )
        }
    }

    private fun registerBiometrics() {
        if (dashboardModel != null) {
            val orgUnit = orgUnitUid ?: return

            val ageInMonths = getAgeInMonthsByAttributes(
                basicPreferenceProvider,
                dashboardModel!!.trackedEntityAttributeValues,
            )

            val orgUnitAsModuleId = getOrgUnitAsModuleId(orgUnit, d2, basicPreferenceProvider)

            if (lastBiometricsSearchSessionId != null){
                view.registerLast(lastBiometricsSearchSessionId)
            } else {
                view.registerBiometrics(
                    orgUnitAsModuleId, dashboardModel!!.trackedEntityInstance.uid(), ageInMonths
                )
            }
        }
    }

    fun handleVerifyResponse(result: VerifyResult) {
        when (result) {
            VerifyResult.Match -> {
                lastVerificationResult = result

                val biometricsValue = dashboardModel?.getBiometricValue()

                if (biometricsValue != null && result == VerifyResult.Match) {
                    teiDataRepository.updateBiometricsAttributeValueInTei(biometricsValue)
                }
            }

            VerifyResult.NoMatch -> {
                lastVerificationResult = result
            }

            VerifyResult.Failure -> {
                lastVerificationResult = result
            }

            VerifyResult.AgeGroupNotSupported -> {
                view.showBiometricsAgeGroupNotSupported()
            }
        }
    }

    fun handleRegisterResponse(result: RegisterResult) {
        lastRegisterResult = result

        when (result) {
            is RegisterResult.Completed -> {
                val biometricsValue = result.guid
                teiDataRepository.updateBiometricsAttributeValueInTei(biometricsValue)
                lastRegisterResult = null
                lastVerificationResult = VerifyResult.Match
                lastPossibleDuplicates = null
            }

            is RegisterResult.Failure -> {
                if (lastDeclinedEnrolDuration > 0) {
                    val lastDeclinedEnrolDurationInMillis =
                        TimeUnit.MINUTES.toMillis(lastDeclinedEnrolDuration.toLong())
                    Timer().schedule(lastDeclinedEnrolDurationInMillis) {
                        lastRegisterResult = null
                    }
                }
            }
            is RegisterResult.RegisterLastFailure -> {
                view.showUnableSaveBiometricsMessage()
            }

            is RegisterResult.AgeGroupNotSupported -> {
                view.showBiometricsAgeGroupNotSupported()
            }

            is RegisterResult.PossibleDuplicates -> {
                onBiometricsPossibleDuplicates(
                    result.items,
                    result.sessionId,
                    enrollNewVisible = true
                )
            }
        }
    }

    private fun refreshVerificationStatus() {
        if ((lastVerificationResult == null || lastVerificationResult == VerifyResult.Match) && dashboardModel != null && dashboardModel!!.isBiometricsEnabled()) {
            val values =
                dashboardRepository.getTEIAttributeValues(programUid, teiUid).blockingSingle()

            val value =
                values.firstOrNull { it.trackedEntityAttribute() == dashboardModel!!.getBiometricsAttributeUid() }


            if (!isLastVerificationValid(
                    value?.lastUpdated(),
                    lastBiometricsVerificationDuration,
                    false
                )
            ) {
                lastVerificationResult = null
            } else {
                lastVerificationResult = VerifyResult.Match
            }

            view.refreshCard()
        }
    }

    fun getBiometricsModel(): TeiDashboardBioModel? {
        return getBiometricsModelByDashboardModel(dashboardModel)
    }

    fun getBiometricsModelByDashboardModel(dashboardModel: DashboardEnrollmentModel?): TeiDashboardBioModel? {
        if (biometricsMode == BiometricsMode.zero) return null

        val teiAttrValues = dashboardModel?.trackedEntityAttributeValues ?: listOf()

        val isUnderAgeThreshold =
            isUnderAgeThreshold(basicPreferenceProvider, teiAttrValues)

        return if (dashboardModel?.isBiometricsEnabled() == true && !isUnderAgeThreshold) {
            val bioValue = dashboardModel!!.getBiometricValue()

            if (bioValue.isNullOrBlank() || (lastRegisterResult != null)) {
                if (biometricsMode == BiometricsMode.limited) return null

                TeiDashboardBioRegistrationMapper(resourceManager).map(
                    lastRegisterResult
                ) {
                    registerBiometrics()
                }
            } else {
                TeiDashboardBioVerificationMapper(resourceManager).map(
                    lastVerificationResult
                ) {
                    verifyBiometrics()
                }
            }
        } else {
            null
        }
    }


    private fun onBiometricsPossibleDuplicates(
        possibleDuplicates: List<SimprintsItem>, sessionId: String,
        enrollNewVisible: Boolean = true
    ) {
        lastRegisterResult = null

        val program = programUid ?: ""
        val biometricsAttUid = biometricAttributeId
        val teiUid = getEnrollment()!!.trackedEntityInstance() ?: ""

        val teiTypeUid = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet()
            ?.trackedEntityType()!!

        val values =
            dashboardRepository.getTEIAttributeValues(programUid, teiUid).blockingSingle()

        val biometricsValue =
            values.firstOrNull { it.trackedEntityAttribute() == dashboardModel!!.getBiometricsAttributeUid() }

        if (possibleDuplicates.isEmpty()) {
            view.registerLast(sessionId)
        } else if (possibleDuplicates.size == 1 && possibleDuplicates[0].guid == biometricsValue?.value()) {
            view.registerLast(sessionId)
        } else {
            val finalPossibleDuplicates =
                possibleDuplicates.filter { it.guid != biometricsValue?.value() }

            lastPossibleDuplicates = LastPossibleDuplicates(finalPossibleDuplicates, sessionId)

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

}
