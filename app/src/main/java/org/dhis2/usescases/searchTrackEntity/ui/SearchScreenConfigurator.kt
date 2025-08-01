package org.dhis2.usescases.searchTrackEntity.ui

import android.transition.TransitionManager
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import org.dhis2.R
import org.dhis2.bindings.display
import org.dhis2.bindings.dp
import org.dhis2.databinding.ActivitySearchBinding
import org.dhis2.usescases.searchTrackEntity.SearchAnalytics
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchScreenState
import org.dhis2.usescases.searchTrackEntity.SearchTEScreenState
import org.dhis2.usescases.searchTrackEntity.ui.BackdropManager.changeBoundsIf
import org.dhis2.utils.isPortrait

class SearchScreenConfigurator(
    val binding: ActivitySearchBinding,
    val filterIsOpenCallback: (isOpen: Boolean) -> Unit,
) {
    fun configure(screenState: SearchTEScreenState) {
        when (screenState) {
            is SearchAnalytics -> configureLandscapeAnalyticsScreen(true)
            is SearchList ->
                if (isPortrait()) {
                    configureListScreen(screenState)
                } else {
                    if (screenState.screenState != SearchScreenState.MAP) {
                        configureLandscapeAnalyticsScreen(false)
                    }
                    configureLandscapeListScreen(screenState)
                }
        }
    }

    private fun configureListScreen(searchConfiguration: SearchList) {
        when {
            searchConfiguration.searchFilters.isOpened -> openFilters()
            searchConfiguration.searchForm.isOpened -> openSearch()
            else -> closeBackdrop()
        }

        binding.clearFilters?.display(searchConfiguration.displayResetFiltersButton())
        syncButtonVisibility(!searchConfiguration.searchForm.isOpened)
        setFiltersVisibility(!searchConfiguration.searchForm.isOpened)
    }

    private fun configureLandscapeListScreen(searchConfiguration: SearchList) {
        if (searchConfiguration.searchFilters.isOpened) {
            openFilters()
        } else if (searchConfiguration.searchForm.isOpened) {
            openSearch()
        } else {
            modifySidePanelContainerWidth(0f)
        }

        syncButtonVisibility(true)
        setFiltersVisibility(true)
    }

    private fun configureLandscapeAnalyticsScreen(expanded: Boolean) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.backdropLayout)
        constraintSet.setGuidelinePercent(R.id.backdropGuideDiv, if (expanded) 0.0f else 0.26f)
        TransitionManager.beginDelayedTransition(binding.backdropLayout)
        constraintSet.applyTo(binding.backdropLayout)
    }

    private fun syncButtonVisibility(canBeDisplayed: Boolean) {
        binding.syncButton.visibility = if (canBeDisplayed) View.VISIBLE else View.GONE
    }

    private fun setFiltersVisibility(showFilters: Boolean) {
        binding.filterCounter.visibility =
            if (showFilters && (binding.totalFilters ?: 0) > 0) View.VISIBLE else View.GONE
        binding.searchFilterGeneral.visibility = if (showFilters) View.VISIBLE else View.GONE
    }

    private fun openFilters() {
        if (isPortrait()) {
            binding.programSpinner.visibility = View.VISIBLE
            binding.title.visibility = View.GONE
        } else {
            modifySidePanelContainerWidth()
        }
        binding.filterRecyclerLayout.visibility = View.VISIBLE
        binding.searchContainer.visibility = View.GONE
        filterIsOpenCallback(true)
        changeBounds(false, R.id.filterRecyclerLayout, 16.dp)
    }

    private fun modifySidePanelContainerWidth(ratio: Float = 0.3f) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.backdropLayout)

        constraintSet.setGuidelinePercent(
            R.id.backdropGuideDiv,
            ratio
        )
        TransitionManager.beginDelayedTransition(binding.backdropLayout)
        constraintSet.applyTo(binding.backdropLayout)
        binding.backdropLayout.requestLayout()
    }

    fun closeBackdrop() {
        if (isPortrait()) {
            binding.programSpinner.visibility = View.VISIBLE
            binding.title.visibility = View.GONE
        }
        binding.filterRecyclerLayout.visibility = View.GONE
        binding.searchContainer.visibility = View.GONE
        filterIsOpenCallback(false)
        changeBounds(true, R.id.backdropGuideTop, 0)
    }

    private fun openSearch() {
        binding.filterRecyclerLayout.visibility = View.GONE
        if (isPortrait()) {
            binding.programSpinner.visibility = View.GONE
            binding.title.visibility = View.VISIBLE
        } else {
            modifySidePanelContainerWidth(0.3f)
        }
        binding.searchContainer.visibility = View.VISIBLE
        filterIsOpenCallback(false)
        changeBounds(false, R.id.searchContainer, 0)
    }

    private fun changeBounds(isNavigationBarVisible: Boolean, endID: Int, margin: Int) {
        changeBoundsIf(
            isPortrait(),
            isNavigationBarVisible,
            binding.backdropLayout,
            endID,
            margin,
        )
    }
}
