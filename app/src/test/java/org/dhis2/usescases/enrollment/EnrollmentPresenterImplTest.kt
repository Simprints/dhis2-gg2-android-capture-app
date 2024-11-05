package org.dhis2.usescases.enrollment

import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.biometrics.BiometricsAttributeUiModelImpl
import org.dhis2.usescases.biometrics.entities.BiometricsMode
import org.dhis2.usescases.enrollment.EnrollmentActivity.EnrollmentMode.CHECK
import org.dhis2.usescases.enrollment.EnrollmentActivity.EnrollmentMode.NEW
import org.dhis2.usescases.teiDashboard.TeiAttributesProvider
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentAccess
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class EnrollmentPresenterImplTest {

    private val enrollmentFormRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: EnrollmentRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    private val analyticsHelper: AnalyticsHelper = mock()
    private val matomoAnalyticsController: MatomoAnalyticsController = mock()
    private val eventCollectionRepository: EventCollectionRepository = mock()
    private val teiAttributesProvider: TeiAttributesProvider = mock()
    private val basicPreferenceProvider: BasicPreferenceProvider = mock()

    @Before
    fun setUp() {
        presenter = EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulers,
            enrollmentFormRepository,
            analyticsHelper,
            matomoAnalyticsController,
            eventCollectionRepository,
            teiAttributesProvider,
            basicPreferenceProvider
        )
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is granted`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(true)
                            .build(),
                    ).build(),
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentRepository).setStatus(EnrollmentStatus.ACTIVE)
        verify(enrollmentView).renderStatus(EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `Check updateEnrollmentStatus where write access is denied`() {
        whenever(programRepository.blockingGet()) doReturn Program.builder().uid("")
            .access(
                Access.builder()
                    .data(
                        DataAccess.builder().write(false)
                            .build(),
                    ).build(),
            ).build()
        presenter.updateEnrollmentStatus(EnrollmentStatus.ACTIVE)

        verify(enrollmentView).displayMessage(null)
    }

    @Test
    fun `Should update the fields flowable`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.updateFields()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should execute the backButton processor`() {
        val processor = PublishProcessor.create<Boolean>()
        val testSubscriber = processor.test()

        presenter.backIsClicked()
        processor.onNext(true)

        testSubscriber.assertValueAt(0, true)
    }

    @Test
    fun `Should show a profile picture image`() {
        val path = "route/image"
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView).displayTeiPicture(path)
    }

    @Test
    fun `Should not show a profile picture image`() {
        val path = ""
        whenever(enrollmentFormRepository.getProfilePicture()) doReturn path
        presenter.onTeiImageHeaderClick()
        verify(enrollmentView, never()).displayTeiPicture(path)
    }

    @Test
    fun `Should show save button when the enrollment is editable and biometrics not available`() {
        setupEnrollmentAccess(EnrollmentAccess.WRITE_ACCESS)
        val nonBiometricsFieldUiModel = mock<FieldUiModel>()
        val fields = listOf(nonBiometricsFieldUiModel)

        presenter.onFieldsLoaded(fields)
        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(true)
    }

    @Test
    fun `Should hide save button when the enrollment is not editable`() {
        setupEnrollmentAccess(EnrollmentAccess.NO_ACCESS)

        presenter.showOrHideSaveButton()

        verify(enrollmentView).setSaveButtonVisible(false)
    }

    @Test
    fun `Should hide save button when biometrics is available`() {
        setupEnrollmentAccess(EnrollmentAccess.WRITE_ACCESS)
        val biometricsFieldUiModel = mock<BiometricsAttributeUiModelImpl>()
        val fields = listOf(biometricsFieldUiModel)

        presenter.onFieldsLoaded(fields)
        presenter.showOrHideSaveButton()

        verify(enrollmentView, atLeastOnce()).setSaveButtonVisible(false)
        verify(enrollmentView, never()).setSaveButtonVisible(true)
    }

    @Test
    fun `Should return true if event status is SCHEDULE`() {
        val event = Event.builder().uid("uid").status(EventStatus.SCHEDULE).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `Should return true if event status is SKIPPED`() {
        val event = Event.builder().uid("uid").status(EventStatus.SKIPPED).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `Should return false if event status is ACTIVE`() {
        val event = Event.builder().uid("uid").status(EventStatus.ACTIVE).build()

        whenever(eventCollectionRepository.uid("uid")) doReturn mock()
        whenever(eventCollectionRepository.uid("uid").blockingGet()) doReturn event
        assert(!presenter.isEventScheduleOrSkipped("uid"))
    }

    @Test
    fun `should create an event right after enrollment creation`() {
        whenever(enrollmentFormRepository.generateEvents()) doReturn Single.just(
            Pair(
                "enrollmentUid",
                "eventUid",
            ),
        )

        presenter.finish(NEW)

        verify(enrollmentView).openEvent("eventUid")
    }

    @Test
    fun `should navigate to enrollment dashboard after enrollment creation`() {
        whenever(enrollmentFormRepository.generateEvents()) doReturn Single.just(
            Pair(
                "enrollmentUid",
                null,
            ),
        )

        presenter.finish(NEW)

        verify(enrollmentView).openDashboard("enrollmentUid")
    }

    @Test
    fun `should close enrollment screen if it already exists`() {
        presenter.finish(CHECK)

        verify(enrollmentView).setResultAndFinish()
    }

    @Test
    fun `Should delete TEI in deleteAllSavedData() when sync state is TO_POST and TEI is not in any other program`() {
        val teiUid = "teiUid"
        val programUid = "programUid"
        givenATei(teiUid, State.TO_POST)
        givenAProgram(programUid)
        givenTeiInNoOtherProgram(teiUid, programUid, true)

        presenter.deleteAllSavedData()

        verify(teiRepository).blockingDelete()
        verify(enrollmentRepository, never()).blockingDelete()
    }

    @Test
    fun `Should only delete enrollment in deleteAllSavedData() when TEI is also in another program`() {
        val teiUid = "teiUid"
        val programUid = "programUid"
        givenATei(teiUid, State.TO_POST)
        givenAProgram(programUid)
        givenTeiInNoOtherProgram(teiUid, programUid, false)

        presenter.deleteAllSavedData()

        verify(enrollmentRepository).blockingDelete()
        verify(teiRepository, never()).blockingDelete()
    }

    @Test
    fun `Should only delete enrollment in deleteAllSavedData() when sync state is not TO_POST`() {
        givenATei("teiUid", State.SYNCED)
        givenAProgram("programUid")

        presenter.deleteAllSavedData()

        verify(enrollmentRepository).blockingDelete()
        verify(teiRepository, never()).blockingDelete()
    }

    private fun setupEnrollmentAccess(access: EnrollmentAccess) {
        val geometry = Geometry.builder()
            .coordinates("[-30.00, 11.00]")
            .type(FeatureType.POINT)
            .build()
        val tei = TrackedEntityInstance.builder().geometry(geometry).uid("random").build()
        val program = Program.builder().uid("tUID").build()

        whenever(teiRepository.blockingGet()) doReturn tei
        whenever(programRepository.blockingGet()) doReturn program
        whenever(d2.enrollmentModule()) doReturn mock()
        whenever(d2.enrollmentModule().enrollmentService()) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollmentService()
                .blockingGetEnrollmentAccess(tei.uid(), program.uid()),
        ) doReturn access
    }

    private fun givenATei(uid: String, syncState: State) {
        val tei = TrackedEntityInstance.builder()
            .uid(uid)
            .syncState(syncState)
            .build()
        whenever(teiRepository.blockingGet()) doReturn tei
    }

    private fun givenAProgram(uid: String) {
        val program = Program.builder().uid(uid).build()
        whenever(programRepository.blockingGet()) doReturn program
    }

    private fun givenTeiInNoOtherProgram(teiUid: String, programUid: String, value: Boolean) {
        whenever(dataEntryRepository.isTeiInNoOtherProgram(teiUid, programUid)) doReturn value
    }

    //EyeSeeTea Customizations
    @Test
    fun `should_not_remove_biometrics_attribute_if_biometrics_mode_is_full`() {
        val presenter = givenABiometricsMode(BiometricsMode.full)

        val fields = givenAFieldsWithBiometrics()

        val finalFields = presenter.onFieldsLoading(fields)

        val biometricsModel = finalFields.find { it is BiometricsAttributeUiModelImpl }

        assertNotNull(biometricsModel)
    }

    @Test
    fun `should_remove_biometrics_attribute_if_biometrics_mode_is_limited`() {
        val presenter = givenABiometricsMode(BiometricsMode.limited)

        val fields = givenAFieldsWithBiometrics()

        val finalFields = presenter.onFieldsLoading(fields)

        val biometricsModel = finalFields.find { it is BiometricsAttributeUiModelImpl }

        assertNull(biometricsModel)
    }

    @Test
    fun `should_remove_biometrics_attribute_if_biometrics_mode_is_zero`() {
        val presenter = givenABiometricsMode(BiometricsMode.zero)

        val fields = givenAFieldsWithBiometrics()

        val finalFields = presenter.onFieldsLoading(fields)

        val biometricsModel = finalFields.find { it is BiometricsAttributeUiModelImpl }

        assertNull(biometricsModel)
    }

    private fun givenABiometricsMode(biometricsMode: BiometricsMode): EnrollmentPresenterImpl {
        whenever(
            basicPreferenceProvider.getString(
                BiometricsPreference.BIOMETRICS_MODE,
                BiometricsMode.full.name
            )
        ).thenReturn(biometricsMode.name)

        return EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulers,
            enrollmentFormRepository,
            analyticsHelper,
            matomoAnalyticsController,
            eventCollectionRepository,
            teiAttributesProvider,
            basicPreferenceProvider
        )
    }

    private fun givenAFieldsWithBiometrics(): List<FieldUiModel> {
        return listOf(
            FieldUiModelImpl(
                uid = "uid",
                layoutId = 0,
                value = null,
                programStageSection = null,
                autocompleteList = null,
                orgUnitSelectorScope = null,
                selectableDates = null,
                eventCategories = null,
                periodSelector = null,
                url = null,
                label = "",
                optionSetConfiguration = null,
                valueType = ValueType.TEXT
            ),
            BiometricsAttributeUiModelImpl(
                uid = "uid",
                layoutId = 0,
                value = null,
                programStageSection = null,
                autocompleteList = null,
                orgUnitSelectorScope = null,
                selectableDates = null,
                eventCategories = null,
                periodSelector = null,
                url = null,
                editable = true,
                ageUnderThreshold = false
            )
        )
    }
}
