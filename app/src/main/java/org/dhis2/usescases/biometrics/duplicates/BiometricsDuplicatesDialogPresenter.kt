package org.dhis2.usescases.biometrics.duplicates

import androidx.paging.map
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.map
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.biometrics.SimprintsItem
import org.dhis2.data.search.SearchParametersModel
import org.dhis2.usescases.searchTrackEntity.SearchRepository
import org.dhis2.usescases.searchTrackEntity.SearchRepositoryKt
import org.dhis2.utils.NetworkUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import timber.log.Timber

class BiometricsDuplicatesDialogPresenter(
    private val d2: D2,
    private val searchRepository: SearchRepository,
    private val searchRepositoryKt: SearchRepositoryKt,
    private val schedulerProvider: SchedulerProvider,
    private val basicPreferenceProvider: BasicPreferenceProvider
) {
    lateinit var view: BiometricsDuplicatesDialogView
    lateinit var possibleDuplicates: List<SimprintsItem>
    lateinit var biometricsSessionId: String
    lateinit var programUid: String
    lateinit var trackedEntityTypeUid: String
    lateinit var biometricsAttributeUid: String

    private var identityConfirmed: Boolean = false

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init(
        view: BiometricsDuplicatesDialogView,
        possibleDuplicates: List<SimprintsItem>,
        biometricsSessionId: String,
        programUid: String,
        trackedEntityTypeUid: String,
        biometricsAttributeUid: String
    ) {
        this.view = view
        this.possibleDuplicates = possibleDuplicates
        this.biometricsSessionId = biometricsSessionId
        this.programUid = programUid
        this.trackedEntityTypeUid = trackedEntityTypeUid
        this.biometricsAttributeUid = biometricsAttributeUid

        loadData()
    }

    private fun loadData() {
        val program = d2.programModule().programs().uid(programUid).blockingGet()

        disposable.add(
            Flowable.just(
                searchRepositoryKt.searchTrackedEntities(
                    SearchParametersModel(
                        program,
                        hashMapOf(biometricsAttributeUid to possibleDuplicates.joinToString(
                            separator = ";"
                        ) { it.guid })
                    ),
                    NetworkUtils.isOnline(view.getContext())
                ).map { pagingData ->
                    pagingData.map { item ->
                        searchRepository.transform(
                            item,
                            program,
                            NetworkUtils.isOnline(view.getContext()),
                            FilterManager.getInstance().sortingItem,
                        )
                    }
                }
            ).doOnError { Timber.e(it) }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { liveData ->
                        view.setLiveData(liveData)
                    },
                    { Timber.e(it) })
        )
    }

    fun onDetach() {
        disposable.clear()
    }

    fun onTEIClick(teiUid: String, enrollmentUid: String, isOnline: Boolean) {
        if (!identityConfirmed) {
            identityConfirmed = true
            sendBiometricsConfirmIdentity(teiUid, enrollmentUid, isOnline)
        } else {
            if (!isOnline) {
                openDashboard(teiUid, enrollmentUid)
            } else {
                downloadTei(teiUid, enrollmentUid)
            }
        }
    }

    private fun sendBiometricsConfirmIdentity(
        teiUid: String,
        enrollmentUid: String,
        isOnline: Boolean
    ) {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues().uid(teiUid).blockingGet()?:return

        val guid: String = getBiometricsValueFromTEI(tei) ?: ""
        view.sendBiometricsConfirmIdentity(
            biometricsSessionId,
            guid,
            teiUid,
            enrollmentUid,
            isOnline,
        )
    }

    private fun getBiometricsValueFromTEI(tei: TrackedEntityInstance): String? {
        var guid: String? = null
        for (att in tei.trackedEntityAttributeValues()!!) {
            if (att.trackedEntityAttribute() == biometricsAttributeUid) {
                guid = att.value()
                break
            }
        }
        return guid
    }

    private fun openDashboard(teiUid: String, enrollmentUid: String) {
        view.openDashboard(
            teiUid,
            programUid,
            enrollmentUid
        )
    }

    private fun downloadTei(teiUid: String?, enrollmentUid: String?) {
        disposable.add(
            searchRepository.downloadTei(teiUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.downloadProgress() }, { Timber.d(it) },
                    {
                        if (d2.trackedEntityModule().trackedEntityInstances().uid(teiUid)
                                .blockingExists()
                        ) {
                            openDashboard(teiUid!!, enrollmentUid!!)
                        } else {
                            val trackedEntityType = d2.trackedEntityModule().trackedEntityTypes()
                                .uid(trackedEntityTypeUid).blockingGet()
                            view.couldNotDownload(trackedEntityType?.displayName()!!)
                        }
                    })
        )
    }

    fun enrollNewClick() {
        view.enrollNew(biometricsSessionId)
    }

    fun enrollWithoutBiometrics() {
        view.enrollWithoutBiometrics()
    }
}
