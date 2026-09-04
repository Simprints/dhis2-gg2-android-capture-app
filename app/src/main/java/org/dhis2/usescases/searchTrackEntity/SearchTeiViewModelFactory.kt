package org.dhis2.usescases.searchTrackEntity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.tracker.search.domain.FetchOptionSetOptions
import org.dhis2.tracker.search.domain.FetchSearchParameters
import org.dhis2.tracker.search.domain.SearchTrackedEntities

class SearchTeiViewModelFactory(
    val presenter: SearchTEContractsModule.Presenter,
    private val searchRepository: SearchRepository,
    private val searchRepositoryKt: SearchRepositoryKt,
    private val searchNavPageConfigurator: SearchPageConfigurator,
    private val initialProgramUid: String?,
    private val initialQuery: MutableMap<String, List<String>?>?,
    private val mapDataRepository: MapDataRepository,
    private val networkUtils: NetworkUtils,
    private val dispatchers: DispatcherProvider,
    private val mapStyleConfig: MapStyleConfiguration,
    private val resourceManager: ResourceManager,
    private val displayNameProvider: DisplayNameProvider,
    private val filterManager: FilterManager,
    private val searchTrackedEntities: SearchTrackedEntities,
    private val fetchSearchParameters: FetchSearchParameters,
    private val fetchOptionSetOptions: FetchOptionSetOptions,
    // EyeSeeTea customization - Age Threshold Controls For Biometrics
    private val basicPreferenceProvider: BasicPreferenceProvider,
    // EyeSeeTea customization - Relationship Search Identification Toggle By TE Type
    private val fromRelationships: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SearchTEIViewModel(
            initialProgramUid,
            initialQuery,
            presenter,
            searchRepository,
            searchRepositoryKt,
            searchNavPageConfigurator,
            mapDataRepository,
            networkUtils,
            dispatchers,
            mapStyleConfig,
            resourceManager,
            displayNameProvider,
            filterManager,
            searchTrackedEntities,
            fetchSearchParameters,
            fetchOptionSetOptions,
            basicPreferenceProvider,
            fromRelationships,
        ) as T
}
