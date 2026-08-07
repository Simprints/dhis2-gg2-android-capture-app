package org.dhis2.usescases.searchTrackEntity.ui.mapper

import android.content.Context
import android.content.SharedPreferences
import org.dhis2.R
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.biometrics.BiometricsPreference
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.date.toOverdueOrScheduledUiText
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.extensions.toJavaDate
import org.dhis2.mobile.commons.extensions.toKtxInstant
import org.dhis2.tracker.input.model.TrackerInputType
import org.dhis2.tracker.search.model.DomainEnrollment
import org.dhis2.tracker.search.model.DomainObjectStyle
import org.dhis2.tracker.search.model.DomainProgram
import org.dhis2.tracker.search.model.EnrollmentStatus
import org.dhis2.tracker.search.model.GeometryFeatureType
import org.dhis2.tracker.search.model.SyncState
import org.dhis2.tracker.search.model.TrackedEntitySearchItemAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntitySearchItemResult
import org.dhis2.tracker.search.model.TrackedEntityTypeAttributeDomain
import org.dhis2.tracker.search.model.TrackedEntityTypeDomain
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.usescases.teiDashboard.ui.mapper.firstNameAttrUid
import org.dhis2.usescases.teiDashboard.ui.mapper.lastNameAttrUid
import org.dhis2.usescases.teiDashboard.ui.mapper.middleNameAttrUid
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Calendar
import java.util.Date
import kotlin.time.Instant

class TEICardMapperTest {
    private val context: Context = mock()
    private val resourceManager: ResourceManager = mock()
    private val sharedPreferences: SharedPreferences = mock()
    private val currentDate = Date()

    private lateinit var mapper: TEICardMapper

    private val enrollmentUid = "EnrollmentUid"
    private val programUid = "programUid"
    private val enrollmentOrgUnit = "OrgUnit"
    private val teiUid = "TEIUid"
    private val selectedEnrollment =      DomainEnrollment(
        uid = enrollmentUid,
        orgUnit = enrollmentOrgUnit,
        program = programUid,
        enrollmentDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        incidentDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        completedDate = Instant.parse("2020-01-01T00:00:00.00Z"),
        followUp = true,
        status = EnrollmentStatus.COMPLETED,
        trackedEntityInstance = teiUid,
    )
    private val enrollments = listOf(
        selectedEnrollment
    )
    @Before
    fun setUp() {
        whenever(context.getString(R.string.interval_now)) doReturn "now"
        whenever(context.getString(R.string.filter_period_today)) doReturn "Today"
        whenever(resourceManager.getString(R.string.show_more)) doReturn "Show more"
        whenever(resourceManager.getString(R.string.show_less)) doReturn "Show less"
        whenever(resourceManager.getString(R.string.completed)) doReturn "Completed"
        whenever(
            resourceManager.formatWithEnrollmentLabel(any(), any(), any(), any()),
        ) doReturn "Enrollment Completed"
        whenever(
            resourceManager.getString(R.string.overdue_today),
        ) doReturn "Today"
        whenever(resourceManager.getString(R.string.marked_follow_up)) doReturn "Marked for follow-up"
        whenever(context.getSharedPreferences(eq("BASIC_SHARE_PREFS"), eq(Context.MODE_PRIVATE))) doReturn sharedPreferences
        whenever(sharedPreferences.getString(eq(BiometricsPreference.BIOMETRICS_MODE), any())) doReturn "full"

        mapper = TEICardMapper(context, resourceManager)
    }

    @Test
    fun shouldReturnCardFull() {
        val model = createFakeModel()

        val result =
            mapper.map(
                searchTEIModel = model,
                onSyncIconClick = {},
                onCardClick = {},
                onImageClick = {},
            )

        assertEquals(result.title, model.header)
        assertEquals(result.lastUpdated, model.tei.lastUpdated?.toJavaDate().toDateSpan(context))
        assertEquals(result.additionalInfo[0].value, model.attributeValues["Name"]?.value)
        assertEquals(result.additionalInfo[1].value, model.tei.ownerOrgUnit)
        assertEquals(result.additionalInfo[2].value, model.tei.enrollmentOrgUnit)
        assertEquals(
            result.additionalInfo[3].value,
            model.tei.enrolledPrograms?.joinToString(", ") { it.displayName },
        )
        assertEquals(
            result.additionalInfo[4].value,
            "Enrollment Completed",
        )

        assertEquals(
            result.additionalInfo[5].value,
            model.tei.overDueDate?.toJavaDate().toOverdueOrScheduledUiText(resourceManager),
        )
        assertEquals(
            result.additionalInfo[6].value,
            resourceManager.getString(R.string.marked_follow_up),
        )
    }

    @Test
    fun shouldShowOverDueLabel() {
        val overdueDate = DateUtils.getInstance().calendar
        overdueDate.add(Calendar.DATE, -2)

        whenever(resourceManager.getPlural(any(), any(), any())) doReturn "2 days"

        val model = createFakeModel(overdueDate.time)

        val result =
            mapper.map(
                searchTEIModel = model,
                onSyncIconClick = {},
                onCardClick = {},
                onImageClick = {},
            )
        assertEquals(
            result.additionalInfo[5].value,
            model.tei.overDueDate?.toJavaDate().toOverdueOrScheduledUiText(resourceManager),
        )
    }

    @Test
    fun `should format confirmation dialog title correctly`() {
        val attributeValues = createAttributeValuesMap(
            firstName = "John",
            middleName = "Peter",
            lastName = "Smith",
        )

        val result = createModelAndMapForConfirmation(attributeValues)

        assertEquals("John Peter Smith", result.title)
    }

    @Test
    fun `should handle empty values in confirmation dialog title`() {
        val attributeValues = createAttributeValuesMap(
            firstName = "John",
            middleName = "-",
            lastName = "Smith",
        )

        val result = createModelAndMapForConfirmation(attributeValues)

        assertEquals("John Smith", result.title)
    }

    @Test
    fun `should return dash when all name values are empty`() {
        val attributeValues = createAttributeValuesMap(
            firstName = "-",
            middleName = "-",
            lastName = "-",
        )

        val result = createModelAndMapForConfirmation(attributeValues)

        assertEquals("-", result.title)
    }

    private fun createAttributeValuesMap(
        firstName: String,
        middleName: String,
        lastName: String,
    ): LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain> = linkedMapOf(
        "First name" to createAttributeValue(firstNameAttrUid, firstName),
        "Middle name" to createAttributeValue(middleNameAttrUid, middleName),
        "Last name" to createAttributeValue(lastNameAttrUid, lastName)
    )

    private fun createModelAndMapForConfirmation(
        attributeValues: LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain>,
    ): ListCardUiModel {
        val model = SearchTeiModel().apply {
            setAttributeValues(attributeValues)
            attributeValues.forEach { (key, value) -> addToAllAttributes(key, value) }
            tei = TrackedEntityInstance.builder()
                .uid("TEIUid")
                .lastUpdated(currentDate)
                .aggregatedSyncState(State.SYNCED)
                .build()
        }
        return mapper.mapForConfirmationDialog(model)
    }

    private fun createAttributeValue(uid: String, value: String): TrackedEntitySearchItemAttributeDomain =
        TrackedEntitySearchItemAttributeDomain(
            attribute = uid,
            displayName = uid,
            displayFormName = uid,
            value = value,
            valueType = TrackerInputType.TEXT,
            displayInList = true,
            optionSet = null,
        )

    private fun createFakeModel(
        currentDate: Date = Date(),
    ): SearchTeiModel {
        val attributeValues = LinkedHashMap<String, TrackedEntitySearchItemAttributeDomain>()
        val attribute =   TrackedEntitySearchItemAttributeDomain(
            attribute = "attrUid1",
            displayName = "Name",
            displayFormName = "Name",
            value = "Peter",
            valueType = TrackerInputType.TEXT,
            displayInList = true,
            optionSet = null
        )
        attributeValues["Name"] = attribute

        val tei = TrackedEntitySearchItemResult(
            uid = "teiUid",
            created = Instant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdated = Instant.parse("2020-01-01T00:00:00.00Z"),
            createdAtClient = Instant.parse("2020-01-01T00:00:00.00Z"),
            lastUpdatedAtClient = Instant.parse("2020-01-01T00:00:00.00Z"),
            ownerOrgUnit = "ownerOrgUnit",
            enrollmentOrgUnit = "enrollmentOrgUnit",
            shouldDisplayOrgUnit = true,
            geometry = null,
            syncState = SyncState.SYNCED,
            aggregatedSyncState = SyncState.SYNCED,
            deleted = false,
            isOnline = true,
            teTypeName = "teTypeName",
            type = TrackedEntityTypeDomain(
                trackedEntityTypeAttributeDomains = listOf(TrackedEntityTypeAttributeDomain(
                    trackedEntityTypeUid=  "trackedEntityTypeUid",
                    trackedEntityAttributeUid = "trackedEntityAttributeUid",
                    displayInList= true,
                    mandatory = false,
                    searchable = true,
                    sortOrder = 1,
                )),
                featureType = GeometryFeatureType.POINT,
            ),
            header = "TEI header",
            overDueDate = currentDate.toKtxInstant(),
            selectedEnrollment = selectedEnrollment,
            profilePicture = null,
            enrolledPrograms = listOf(
                DomainProgram(
                uid = "Program1Uid",
                displayName = "Program 1",
                style = DomainObjectStyle(
                    icon = "iconUid",
                    color = "colorUid"
                )
                setAttributeValues(attributeValues)
                // EyeSeeTea customization - Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form
                attributeValues.forEach { (key, value) -> addToAllAttributes(key, value) }

        return searchTeiModel
    }
}
