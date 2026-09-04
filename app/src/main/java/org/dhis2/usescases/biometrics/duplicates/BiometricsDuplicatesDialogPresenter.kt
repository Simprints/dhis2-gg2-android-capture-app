package org.dhis2.usescases.biometrics.duplicates

import androidx.paging.map
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometrics.biometricsClient.models.SimprintsIdentifiedItem
import org.dhis2.tracker.search.domain.SearchTrackedEntities
import org.dhis2.tracker.search.model.QueryData
import org.dhis2.tracker.search.model.SearchTrackedEntitiesInput
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
    private val basicPreferenceProvider: BasicPreferenceProvider,
    // EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
    // Baseline's coroutine use case, replacing the removed SearchRepositoryKt.searchTrackedEntities.
    private val searchTrackedEntities: SearchTrackedEntities,
    private val dispatchers: DispatcherProvider,
) {
    // EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
    // Scope for the migrated coroutine search; cancelled in onDetach() alongside the RxJava
    // disposable still used by downloadTei().
    private val scope = CoroutineScope(SupervisorJob() + dispatchers.io())

    lateinit var view: BiometricsDuplicatesDialogView
    lateinit var possibleDuplicates: List<SimprintsIdentifiedItem>
    lateinit var biometricsSessionId: String
    lateinit var programUid: String
    lateinit var trackedEntityTypeUid: String
    lateinit var biometricsAttributeUid: String

    private var identityConfirmed: Boolean = false

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init(
        view: BiometricsDuplicatesDialogView,
        possibleDuplicates: List<SimprintsIdentifiedItem>,
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

    // EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
    // Resolves the Simprints candidate GUIDs into TEIs through the normal DHIS2 search, so the
    // duplicates list is backed by real search results instead of a separate local lookup.
    // Migrated from RxJava to the coroutine-based SearchTrackedEntities use case in 3.4.1,
    // mirroring how SearchTEIViewModel now performs a search.
    private fun loadData() {
        scope.launch {
            try {
                val input =
                    SearchTrackedEntitiesInput(
                        selectedProgram = programUid,
                        allowCache = false,
                        excludeValues = null,
                        hasStateFilters = false,
                        isOnline = NetworkUtils.isOnline(view.getContext()),
                        queryDataList =
                            listOf(
                                QueryData(
                                    attributeId = biometricsAttributeUid,
                                    values = possibleDuplicates.map { it.guid },
                                    searchOperator = null,
                                ),
                            ),
                    )

                val results = searchTrackedEntities.invoke(input).getOrThrow()

                view.setLiveData(
                    results.map { pagingData ->
                        pagingData.map { item ->
                            searchRepositoryKt.mapTrackedEntitySearchItemResultToSearchTeiModel(
                                item,
                                FilterManager.getInstance().sortingItem,
                            )
                        }
                    },
                )
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun onDetach() {
        disposable.clear()
        // EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
        scope.cancel()
    }

    fun onTEIClick(teiUid: String, enrollmentUid: String?, isOnline: Boolean) {
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
        enrollmentUid: String?,
        isOnline: Boolean
    ) {
        val tei = d2.trackedEntityModule().trackedEntityInstances()
            .withTrackedEntityAttributeValues().uid(teiUid).blockingGet()?:return

        val guid: String = getBiometricsValueFromTEI(tei) ?: ""

        searchRepository.updateAttributeValue(teiUid, biometricsAttributeUid, guid)

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

    private fun openDashboard(teiUid: String, enrollmentUid: String?) {
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
