package org.dhis2.usescases.biometrics.duplicates

import android.content.Context
import androidx.paging.PagingData
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.Flow
import org.dhis2.commons.data.SearchTeiModel
import org.hisp.dhis.android.core.arch.call.D2Progress

interface BiometricsDuplicatesDialogView {
    fun setLiveData(liveData: Flow<PagingData<SearchTeiModel>>)
    fun getContext(): Context?
    fun openDashboard(teiUid: String, programUid: String, enrollmentUid: String)
    fun downloadProgress(): Consumer<D2Progress>
    fun couldNotDownload(displayName: String)
    fun enrollNew(biometricsSessionId: String)
    fun enrollWithoutBiometrics()
    fun sendBiometricsConfirmIdentity(
        sessionId: String, guid: String, teiUid: String, enrollmentUid: String, isOnline: Boolean
    )
}
