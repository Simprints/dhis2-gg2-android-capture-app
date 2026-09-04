package org.dhis2.usescases.enrollment

import org.dhis2.commons.data.TeiAttributesInfo
import org.dhis2.data.biometrics.biometricsClient.models.SimprintsIdentifiedItem
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus

interface EnrollmentView : AbstractActivityContracts.View {
    fun setAccess(access: Boolean?)

    fun renderStatus(status: EnrollmentStatus)

    fun setSaveButtonVisible(visible: Boolean)

    fun displayTeiInfo(teiInfo: TeiAttributesInfo)

    fun openEvent(eventUid: String)

    fun openDashboard(enrollmentUid: String)

    fun goBack()

    fun setResultAndFinish()

    fun requestFocus()

    fun performSaveClick()

    // EyeSeeTea customization - Biometrics In TEI Cards, TEI Dashboard, Enrollment, And TEI Form
    // Submits a FormIntent directly to FormView's ViewModel, bypassing FieldUiModel.Callback
    // (see FormView.submitIntent for why the callback can be stale/null on a fork-held model).
    fun submitFormIntent(intent: FormIntent)

    fun displayTeiPicture(picturePath: String)

    fun showDateEditionWarning(message: String?)
    fun registerBiometrics(
        moduleId: String,
        ageInMonths: Long,
        trackedEntityInstanceId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>,
    )

    fun showPossibleDuplicatesDialog(
        guids: List<SimprintsIdentifiedItem>,
        sessionId: String,
        programUid: String,
        trackedEntityTypeUid: String,
        biometricsAttributeUid: String,
        enrollNewVisible: Boolean,
        moduleId: String,
        ageInMonths: Long?,
        trackedEntityInstanceId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>,
    )

    fun registerLast(
        sessionId: String,
        moduleId: String,
        ageInMonths: Long?,
        trackedEntityInstanceId: String,
        enrollingOrgUnitId: String,
        enrollingOrgUnitName: String,
        userOrgUnits: List<String>,
    )

    fun markAsPendingSave()
    fun showUnableSaveBiometricsMessage()
    fun showBiometricsAgeGroupNotSupported()
}
