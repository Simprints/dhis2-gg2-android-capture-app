package org.dhis2.usescases.enrollment

import org.dhis2.commons.data.TeiAttributesInfo
import org.dhis2.data.biometrics.SimprintsIdentifiedItem
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
}
