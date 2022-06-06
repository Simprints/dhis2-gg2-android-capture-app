package org.dhis2.data.dhislogic

import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

class EnrollmentEventGeneratorRepositoryImpl(private val d2: D2) :
    EnrollmentEventGeneratorRepository {
    override fun enrollment(enrollmentUid: String): Enrollment {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()
    }

    override fun enrollmentAutogeneratedEvents(
        enrollmentUid: String,
        programUid: String
    ): List<ProgramStage> {
        return d2.programModule().programStages()
            .byProgramUid().eq(programUid)
            .byAutoGenerateEvent().isTrue
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet()
    }

    override fun enrollmentProgram(programUid: String): Program {
        return d2.programModule().programs().uid(programUid).blockingGet()
    }

    override fun firstStagesInProgram(programUid: String): ProgramStage? {
        return d2.programModule().programStages().byProgramUid().eq(programUid)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet().firstOrNull()
    }

    override fun firstOpenAfterEnrollmentStage(programUid: String): ProgramStage? {
        return d2.programModule().programStages().byProgramUid().eq(programUid)
            .byOpenAfterEnrollment().isTrue
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet().firstOrNull()
    }

    override fun eventExistInEnrollment(enrollmentUid: String, stageUid: String): Boolean {
        return d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(stageUid)
            .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
            .one().blockingExists()
    }

    override fun eventUidInEnrollment(enrollmentUid: String, stageUid: String): String {
        return d2.eventModule().events()
            .byEnrollmentUid().eq(enrollmentUid)
            .byProgramStageUid().eq(stageUid)
            .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
            .one().blockingGet().uid()
    }

    override fun addEvent(
        enrollmentUid: String,
        programUid: String,
        stageUid: String,
        orgUnitUid: String
    ): String {
        val eventToAdd = EventCreateProjection.builder()
            .enrollment(enrollmentUid)
            .program(programUid)
            .programStage(stageUid)
            .attributeOptionCombo(null)
            .organisationUnit(orgUnitUid)
            .build()
        return d2.eventModule().events().blockingAdd(eventToAdd)
    }

    override fun periodStartingDate(periodType: PeriodType, date: Date): Date {
        return d2.periodModule().periodHelper()
            .blockingGetPeriodForPeriodTypeAndDate(periodType, date)
            .startDate()!!
    }

    override fun setEventDate(eventUid: String, isScheduled: Boolean, date: Date) {
        val eventRepository = d2.eventModule().events().uid(eventUid)

        if (isScheduled) {
            eventRepository.setDueDate(date)
            eventRepository.setStatus(EventStatus.SCHEDULE)
        } else {
            eventRepository.setEventDate(date)
        }
    }
}