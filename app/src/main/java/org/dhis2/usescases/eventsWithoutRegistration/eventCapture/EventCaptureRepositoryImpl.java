package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import static org.dhis2.data.biometrics.utils.GetBiometricsTrackedEntityAttributeKt.getBiometricsTrackedEntityAttribute;
import static org.dhis2.data.biometrics.utils.GetTeiByUidKt.getTeiByUid;
import static org.dhis2.data.biometrics.utils.GetTrackedEntityAttributeValueByAttributeKt.getTrackedEntityAttributeValueByAttribute;
import static org.dhis2.data.biometrics.utils.UpdateBiometricsAttributeValueKt.updateBiometricsAttributeValue;
import static org.dhis2.usescases.biometrics.AgeInMonthsKt.getAgeInMonthsByAttributes;
import static org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureRepositoryFunctionsKt.getProgramStageName;

import org.dhis2.commons.prefs.BasicPreferenceProvider;
import org.dhis2.commons.bindings.SdkExtensionsKt;
import org.dhis2.data.dhislogic.AuthoritiesKt;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ValidationStrategy;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.event.EventNonEditableReason;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final String eventUid;
    private final D2 d2;

    private final BasicPreferenceProvider basicPreferenceProvider;

    public EventCaptureRepositoryImpl(String eventUid, D2 d2, BasicPreferenceProvider basicPreferenceProvider) {
        this.eventUid = eventUid;
        this.d2 = d2;
        this.basicPreferenceProvider = basicPreferenceProvider;
    }

    private Event getCurrentEvent() {
        return d2.eventModule().events().uid(eventUid).blockingGet();
    }

    @Override
    public boolean isEnrollmentOpen() {
        Event currentEvent = getCurrentEvent();
        return currentEvent.enrollment() == null || d2.enrollmentModule().enrollmentService().blockingIsOpen(currentEvent.enrollment());
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(getCurrentEvent().enrollment()).blockingGet();
        if (enrollment == null)
            return false;
        else
            return enrollment.status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventEditable(String eventUid) {
        return d2.eventModule().eventService().blockingIsEditable(eventUid);
    }

    @Override
    public Flowable<String> programStageName() {
        return Flowable.just(getProgramStageName(d2, eventUid));
    }

    @Override
    public Flowable<OrganisationUnit> orgUnit() {
        return Flowable.just(
                Objects.requireNonNull(
                        d2.organisationUnitModule()
                                .organisationUnits()
                                .uid(getCurrentEvent().organisationUnit())
                                .blockingGet()
                )
        );
    }

    @Override
    public Observable<Boolean> completeEvent() {
        return Observable.fromCallable(() -> {
            try {
                d2.eventModule().events().uid(eventUid).setStatus(EventStatus.COMPLETED);
                return true;
            } catch (D2Error d2Error) {
                Timber.e(d2Error);
                return false;
            }
        });
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        return d2.eventModule().events().uid(eventUid).delete()
                .andThen(Observable.just(true));
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {

        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(eventUid)
                    .setStatus(status);
            return true;
        });
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(eventUid)
                    .setDueDate(newDate);
            d2.eventModule().events().uid(eventUid)
                    .setStatus(EventStatus.SCHEDULE);
            return true;
        });
    }

    @Override
    public Observable<String> programStage() {
        return Observable.just(Objects.requireNonNull(getCurrentEvent().programStage()));
    }

    @Override
    public boolean getAccessDataWrite() {
        return d2.eventModule().eventService().blockingHasDataWriteAccess(eventUid);
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(Objects.requireNonNull(getCurrentEvent().status()));
    }

    @Override
    public Single<Boolean> canReOpenEvent() {
        return Single.fromCallable(() -> d2.userModule().authorities()
                .byName().in(AuthoritiesKt.AUTH_UNCOMPLETE_EVENT, AuthoritiesKt.AUTH_ALL).one().blockingExists()
        );
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return d2.eventModule().eventService().getEditableStatus(eventUid).map(editionStatus -> {
            if (editionStatus instanceof EventEditableStatus.NonEditable nonEditableStatus) {
                return nonEditableStatus.getReason() == EventNonEditableReason.EXPIRED;
            } else {
                return false;
            }
        }).toObservable();
    }

    @Override
    public Flowable<Boolean> eventIntegrityCheck() {
        Event currentEvent = getCurrentEvent();
        return Flowable.just(currentEvent).map(event ->
                (event.status() == EventStatus.COMPLETED ||
                        event.status() == EventStatus.ACTIVE) &&
                        (event.eventDate() == null || !event.eventDate().after(new Date()))
        );
    }

    @Override
    public Single<Integer> getNoteCount() {
        return d2.noteModule().notes().byEventUid().eq(eventUid).count();
    }

    @Override
    public boolean showCompletionPercentage() {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            ProgramConfigurationSetting programConfigurationSetting = d2.settingModule()
                    .appearanceSettings()
                    .getProgramConfigurationByUid(getCurrentEvent().program());

            if (programConfigurationSetting != null &&
                    programConfigurationSetting.completionSpinner() != null) {
                return programConfigurationSetting.completionSpinner();
            }
        }
        return true;
    }

    @Override
    public boolean hasAnalytics() {
        Event currentEvent = getCurrentEvent();
        boolean hasProgramIndicators = !d2.programModule().programIndicators().byProgramUid().eq(currentEvent.program()).blockingIsEmpty();
        List<ProgramRule> programRules = d2.programModule().programRules().withProgramRuleActions()
                .byProgramUid().eq(currentEvent.program()).blockingGet();
        boolean hasProgramRules = false;
        for (ProgramRule rule : programRules) {
            for (ProgramRuleAction action : Objects.requireNonNull(rule.programRuleActions())) {
                if (action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR ||
                        action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT) {
                    hasProgramRules = true;
                }
            }
        }
        return hasProgramIndicators || hasProgramRules;
    }

    @Override
    public boolean hasRelationships() {
        return !d2.relationshipModule().relationshipTypes()
                .byAvailableForEvent(eventUid)
                .blockingIsEmpty();
    }

    @Override
    public ValidationStrategy validationStrategy() {
        ValidationStrategy validationStrategy =
                SdkExtensionsKt.programStage(d2, programStage().blockingFirst())
                        .validationStrategy();

        return validationStrategy != null ? validationStrategy : ValidationStrategy.ON_COMPLETE;
    }

    @Override
    public Date getBiometricsAttributeValueInTeiLastUpdated() {
        Date lastUpdated = null;

        TrackedEntityInstance tei = getTeiByEvent(getCurrentEvent());

        String attributeUid = getBiometricsTrackedEntityAttribute(d2);

        TrackedEntityAttributeValue attValue =
                getTrackedEntityAttributeValueByAttribute(attributeUid, tei.trackedEntityAttributeValues());

        if (attValue != null) {
            lastUpdated = attValue.lastUpdated();
        }

        return lastUpdated;
    }

    @Override
    public void updateBiometricsAttributeValueInTei(String biometricsGuid) {
        TrackedEntityInstance tei = getTeiByEvent(getCurrentEvent());

        updateBiometricsAttributeValue(d2, tei.uid(), biometricsGuid);
    }

    private TrackedEntityInstance getTeiByEvent(Event currentEvent) {
        String enrollmentUid = currentEvent.enrollment();

        if (enrollmentUid != null) {
            Enrollment enrollment = d2.enrollmentModule().enrollments()
                    .uid(enrollmentUid).blockingGet();

            return getTeiByUid(d2, enrollment.trackedEntityInstance());
        } else {
            return null;
        }
    }

    @Override
    public Observable<Long> getAgeInMonths() {
        TrackedEntityInstance tei = getTeiByEvent(getCurrentEvent());

        Long ageInMonths = getAgeInMonthsByAttributes(basicPreferenceProvider, tei.trackedEntityAttributeValues());
        return Observable.just(ageInMonths);
    }
}

