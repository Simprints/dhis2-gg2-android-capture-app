package org.dhis2.usescases.main.program

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.featureconfig.model.FeatureOptions
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.usescases.biometrics.BIOMETRICS_ENABLED
import org.dhis2.usescases.biometrics.usecases.SelectBiometricsConfig
import timber.log.Timber

class ProgramViewModel internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val featureConfigRepository: FeatureConfigRepository,
    private val dispatchers: DispatcherProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
    private val selectBiometricsConfig: SelectBiometricsConfig
) : ViewModel() {

    private val _programs = MutableLiveData<List<ProgramUiModel>>()
    val programs: LiveData<List<ProgramUiModel>> = _programs

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        programRepository.clearCache()
        fetchPrograms()
    }

    private fun fetchPrograms() {
        viewModelScope.launch {
            val result = async(dispatchers.io()) {
                val programs = programRepository.homeItems(
                    syncStatusController.observeDownloadProcess().value,
                ).blockingLast()
                if (featureConfigRepository.isFeatureEnable(Feature.RESPONSIVE_HOME)) {
                    val feature = featureConfigRepository.featuresList.find { it.feature == Feature.RESPONSIVE_HOME }
                    val totalItems = feature?.extras?.takeIf { it is FeatureOptions.ResponsiveHome }?.let {
                        it as FeatureOptions.ResponsiveHome
                        it.totalItems
                    }
                    programs.take(
                        totalItems ?: programs.size,
                    )
                } else {
                    programs
                }
            }
            try {
                _programs.postValue(result.await())
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun onSyncStatusClick(program: ProgramUiModel) {
        val programTitle = "$CLICK_ON${program.title}"
        matomoAnalyticsController.trackEvent(HOME, SYNC_BTN, programTitle)
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        init()
    }

    fun onItemClick(programModel: ProgramUiModel) {
        if (BIOMETRICS_ENABLED) {
            viewModelScope.launch (dispatchers.io()) {
                selectBiometricsConfig(programModel.uid).collect {
                    // Handle the completion of the flow if needed
                }
            }
        }

        view.navigateTo(programModel)
    }

    fun dispose() {
        disposable.clear()
    }

    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        viewModelScope.launch(dispatchers.io()) {
            fetchPrograms()
        }
    }
}
