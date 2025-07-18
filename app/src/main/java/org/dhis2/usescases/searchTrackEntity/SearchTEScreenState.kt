package org.dhis2.usescases.searchTrackEntity

import org.dhis2.usescases.biometrics.entities.BiometricsMode

sealed class SearchTEScreenState(
    val screenState: SearchScreenState,
    open val previousSate: SearchScreenState,
)

data class SearchList(
    override val previousSate: SearchScreenState,
    private val listType: SearchScreenState,
    val displayFrontPageList: Boolean,
    val canCreateWithoutSearch: Boolean,
    val isSearching: Boolean,
    val searchForm: SearchForm,
    val searchFilters: SearchFilters,
    val biometricsMode: BiometricsMode
) : SearchTEScreenState(listType, previousSate) {

    fun displayResetFiltersButton(): Boolean {
        return searchFilters.isOpened and searchFilters.hasActiveFilters
    }
}

data class SearchForm(
    val queryHasData: Boolean,
    val minAttributesToSearch: Int,
    val isForced: Boolean = false,
    val isOpened: Boolean = false,
)

data class SearchFilters(
    val hasActiveFilters: Boolean = false,
    val isOpened: Boolean = false,
)

data class SearchAnalytics(
    override val previousSate: SearchScreenState,
) : SearchTEScreenState(SearchScreenState.ANALYTICS, previousSate)

enum class SearchScreenState {
    NONE,
    LIST,
    MAP,
    ANALYTICS,
}
