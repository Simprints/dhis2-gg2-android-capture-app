package org.dhis2.usescases.enrollment

import org.dhis2.commons.data.TeiAttributesInfo
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
    fun showProgress()
    fun hideProgress()
    fun displayTeiPicture(picturePath: String)
    fun showDateEditionWarning()
    fun registerBiometrics(orgUnit: String, ageInMonths: Long)
    fun showPossibleDuplicatesDialog(
        guids: List<String>,
        sessionId: String,
        programUid: String,
        trackedEntityTypeUid: String,
        biometricsAttributeUid: String
    )

    fun registerLast(sessionId: String)
}
