package org.dhis2.usescases.searchTrackEntity.listView

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.bindings.dp
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.filters.workingLists.WorkingListViewModelFactory
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.FragmentSearchListBinding
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.usescases.biometrics.ui.SequentialNextSearchActions
import org.dhis2.usescases.biometrics.ui.SequentialSearch
import org.dhis2.usescases.biometrics.ui.SequentialSearchAction
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.SearchList
import org.dhis2.usescases.searchTrackEntity.SearchTEActivity
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiModel
import org.dhis2.usescases.searchTrackEntity.SearchTeiViewModelFactory
import org.dhis2.usescases.searchTrackEntity.adapters.SearchTeiLiveAdapter
import org.dhis2.usescases.searchTrackEntity.ui.CreateNewButton
import org.dhis2.usescases.searchTrackEntity.ui.FullSearchButtonAndWorkingList
import org.dhis2.usescases.searchTrackEntity.ui.LoadingContent
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.dhis2.utils.isLandscape
import timber.log.Timber
import javax.inject.Inject

const val ARG_FROM_RELATIONSHIP = "ARG_FROM_RELATIONSHIP"
private const val DIRECTION_DOWN = 1

class SearchTEList : FragmentGlobalAbstract() {

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var workingListViewModelFactory: WorkingListViewModelFactory

    @Inject
    lateinit var colorUtils: ColorUtils

    @Inject
    lateinit var teiCardMapper: TEICardMapper

    private val viewModel by activityViewModels<SearchTEIViewModel> { viewModelFactory }

    private val workingListViewModel by viewModels<WorkingListViewModel> { workingListViewModelFactory }

    private val KEY_SCROLL_POSITION = "scroll_position"
    private val KEY_LAST_CLICKED_TEI_UID = "last_clicked_tei_uid"

    private val initialLoadingAdapter by lazy {
        SearchListResultAdapter { }
    }

    private lateinit var recycler: RecyclerView

    private val liveAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            colorUtils,
            cardMapper = teiCardMapper,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail,
            onSearchTeiModelClick = ::onSearchTeiModelClick,
        )
    }


    private val globalAdapter by lazy {
        SearchTeiLiveAdapter(
            fromRelationship,
            colorUtils,
            cardMapper = teiCardMapper,
            onAddRelationship = viewModel::onAddRelationship,
            onSyncIconClick = viewModel::onSyncIconClick,
            onDownloadTei = viewModel::onDownloadTei,
            onTeiClick = viewModel::onTeiClick,
            onImageClick = ::displayImageDetail,
            onSearchTeiModelClick = viewModel::onSearchTeiModelClick,
        )
    }

    private val resultAdapter by lazy {
        SearchListResultAdapter {
            initGlobalData()
        }
    }

    private val listAdapter by lazy {
        ConcatAdapter(initialLoadingAdapter, liveAdapter, globalAdapter, resultAdapter)
    }

    private val fromRelationship by lazy {
        arguments?.getBoolean(ARG_FROM_RELATIONSHIP) ?: false
    }

    private var currentLastClickedTeiUid: String? = null

    companion object {
        fun get(fromRelationships: Boolean): SearchTEList {
            return SearchTEList().apply {
                arguments = bundleArguments(fromRelationships)
            }
        }
    }

    private fun bundleArguments(fromRelationships: Boolean): Bundle {
        return Bundle().apply {
            putBoolean(ARG_FROM_RELATIONSHIP, fromRelationships)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context as SearchTEActivity).searchComponent?.plus(
            SearchTEListModule(),
        )?.inject(this)
    }

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return FragmentSearchListBinding.inflate(inflater, container, false).apply {
            configureList(
                scrollView,
                savedInstanceState?.getInt(KEY_SCROLL_POSITION),
                savedInstanceState?.getString(KEY_LAST_CLICKED_TEI_UID)
            )

            configureOpenSearchButton(openSearchButton)

            //EyeSeeTea customization
            configureCreateButton(createButton)
            configureSequentialSearchNextAction(nextActions)
            configureBiometricsLoader(biometricsLoader)
            configureRecyclerVisibility()
        }.root.also {
            observeNewData()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val layoutManager = recycler.layoutManager as? LinearLayoutManager
        layoutManager?.let {
            outState.putInt(KEY_SCROLL_POSITION, it.findFirstCompletelyVisibleItemPosition())
        }

        if (currentLastClickedTeiUid != null) {
            outState.putString(KEY_LAST_CLICKED_TEI_UID, currentLastClickedTeiUid)
        }
    }

    private fun configureList(
        scrollView: RecyclerView,
        currentVisiblePosition: Int?,
        lastClickedTeiUid: String?,
    ) {
        var currentPosition = currentVisiblePosition
        currentLastClickedTeiUid = lastClickedTeiUid

        val layoutManager = scrollView.layoutManager as? LinearLayoutManager
        scrollView.apply {
            adapter = listAdapter
            // Deactivate ItemAnimator to avoid crash:
            // java.lang.IllegalArgumentException: Tmp detached view should be removed from RecyclerView before it can be recycled
            itemAnimator = null
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        CoroutineTracker.decrement()
                    } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        CoroutineTracker.increment()
                    }
                    if (!recyclerView.canScrollVertically(DIRECTION_DOWN)) {
                        viewModel.isScrollingDown.value = false
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy > 0) {
                        viewModel.isScrollingDown.value = true
                        currentPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                    } else if (dy < 0) {
                        viewModel.isScrollingDown.value = false
                        currentPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                    }
                }
            })
            lifecycleScope.launch {
                liveAdapter.loadStateFlow.collectLatest {
                    if (currentLastClickedTeiUid != null) {
                        val position =
                            liveAdapter.snapshot().items.indexOfFirst { it.tei.uid() == currentLastClickedTeiUid }
                        if (position != -1) {
                            layoutManager?.scrollToPositionWithOffset(position, 0)
                        }
                    } else {
                        scrollToPosition(currentPosition ?: 0)
                    }
                }
            }
        }.also {
            recycler = it
        }
    }

    @ExperimentalAnimationApi
    private fun configureOpenSearchButton(openSearchButton: ComposeView) {
        openSearchButton.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val teTypeName by viewModel.teTypeName.observeAsState()
                val sequentialSearch by viewModel.sequentialSearch.observeAsState(false)
                val isLoaded by viewModel.isDataLoaded.observeAsState(false)
                val screenState by viewModel.screenState.observeAsState()

                val seqSearch = (sequentialSearch as SequentialSearch?)

                if (seqSearch == null && !teTypeName.isNullOrBlank() && isLoaded == true) {

                    val isSearchByBiometrics =
                        if (screenState is SearchList) viewModel.isSearchByBiometricsEnabled() else false

                    val isFilterOpened by viewModel.filtersOpened.observeAsState(false)
                    val createButtonVisibility by viewModel
                        .createButtonScrollVisibility.observeAsState(true)
                    val queryData = remember(viewModel.uiState) {
                        viewModel.uiState.searchedItems
                    }

                    FullSearchButtonAndWorkingList(
                        teTypeName = teTypeName!!,
                        modifier = Modifier,
                        createButtonVisible = createButtonVisibility,
                        closeFilterVisibility = isFilterOpened,
                        isLandscape = isLandscape() && !isSearchByBiometrics,
                        queryData = queryData,
                        onSearchClick = {
                            if (isSearchByBiometrics) viewModel.sequentialSearchNextAction(
                                SequentialSearchAction.SearchWithBiometrics
                            ) else viewModel.setSearchScreen()
                        },
                        onEnrollClick = { viewModel.onEnrollClick() },
                        onCloseFilters = { viewModel.onFiltersClick(isLandscape()) },
                        onClearSearchQuery = {
                            viewModel.clearQueryData()
                            viewModel.clearFocus()
                        },
                        workingListViewModel = workingListViewModel,
                    )
                }
            }
        }
    }

    @ExperimentalAnimationApi
    private fun configureCreateButton(createButton: ComposeView) {
        createButton.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val isScrollingDown by viewModel.isScrollingDown.observeAsState(false)
                val createButtonVisibility by viewModel
                    .createButtonScrollVisibility.observeAsState(true)
                val filtersOpened by viewModel.filtersOpened.observeAsState(false)
                val teTypeName by viewModel.teTypeName.observeAsState()
                val hasQueryData = remember(viewModel.uiState) {
                    viewModel.queryData.isNotEmpty()
                }
                val sequentialSearch by viewModel.sequentialSearch.observeAsState()

                val newPatientAction = sequentialSearch?.nextActions?.firstOrNull {
                    it == SequentialSearchAction.RegisterNew
                }

                updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    val bottomMargin = if (viewModel.isBottomNavigationBarVisible()) {
                        56.dp
                    } else {
                        16.dp
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }

                val orientation = LocalConfiguration.current.orientation
                if (newPatientAction != null && (hasQueryData || orientation == Configuration.ORIENTATION_LANDSCAPE) && createButtonVisibility && !filtersOpened && !teTypeName.isNullOrBlank()) {
                    CreateNewButton(
                        modifier = Modifier,
                        extended = !isScrollingDown,
                        onClick = { viewModel.sequentialSearchNextAction(newPatientAction) },
                        teTypeName = teTypeName!!,
                    )
                }
            }
        }
    }


    private fun displayImageDetail(imagePath: String) {
        val intent = ImageDetailActivity.intent(
            context = requireContext(),
            title = null,
            imagePath = imagePath,
        )

        startActivity(intent)
    }

    private fun observeNewData() {
        initData()
        viewModel.refreshData.observe(viewLifecycleOwner) {
            restoreAdapters()
        }

        viewModel.dataResult.observe(viewLifecycleOwner) {
            initLoading(emptyList())
            it.firstOrNull()?.let { searchResult ->
                if (searchResult.shouldClearProgramData()) {
                    liveAdapter.refresh()
                }
                if (searchResult.shouldClearGlobalData()) {
                    globalAdapter.refresh()
                }
                if (searchResult.type == SearchResult.SearchResultType.TOO_MANY_RESULTS) {
                    listAdapter.removeAdapter(liveAdapter)
                }
                displayResult(it)
                updateRecycler()
            }
        }

        liveAdapter.addLoadStateListener { state ->
            /* EyeSeTea customization - Show loader when loading new results
                if (state.append == LoadState.Loading) {
                displayResult(
                    listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
                )
            } else {
                displayResult(null)
             */
            when {
                state.refresh == LoadState.Loading -> {
                    displayResult(
                        listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
                    )
                }

                state.append == LoadState.Loading -> {
                    displayResult(
                        listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
                    )
                }

                else -> {
                    displayResult(null)
                }
            }
        }

        scrollToTopOnSequentialSearch()
    }

    private fun scrollToTopOnSequentialSearch() {
        viewModel.sequentialSearch.observe(viewLifecycleOwner) {
            recycler.scrollToPosition(0)
        }
    }

    private fun updateRecycler() {
        recycler.setPaddingRelative(
            0,
            0,
            0,
            when {
                listAdapter.itemCount > 1 -> 160.dp
                else -> 0.dp
            },
        )
    }

    private fun restoreAdapters() {
        if (!listAdapter.adapters.contains(liveAdapter)) {
            listAdapter.addAdapter(1, liveAdapter)
        }
        initLoading(null)
        liveAdapter.refresh()
        if (!viewModel.filtersApplyOnGlobalSearch()) {
            globalAdapter.refresh()
        } else if (globalAdapter.itemCount > 0) {
            initGlobalData()
        }
        displayResult(null)
    }

    private fun initData() {
        displayLoadingData()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchPagingData.collect { data ->
                    liveAdapter.addOnPagesUpdatedListener {
                        onInitDataLoaded()

                        viewModel.verifyAutoNavigateToTEI(liveAdapter.snapshot().items)

                        CoroutineTracker.decrement()
                    }
                    liveAdapter.submitData(lifecycle, data)
                }
            }
        }
    }

    private fun onInitDataLoaded() {
        viewModel.onDataLoaded(
            programResultCount = liveAdapter.itemCount,
            globalResultCount = if (globalAdapter.itemCount > 0) {
                globalAdapter.itemCount
            } else {
                null
            },
            onlineErrorCode = liveAdapter.snapshot().items.lastOrNull()?.onlineErrorCode,
        )
        hideToolBarProgress()
    }

    private fun onGlobalDataLoaded() {
        viewModel.onDataLoaded(
            programResultCount = liveAdapter.itemCount,
            globalResultCount = globalAdapter.itemCount,
        )
        hideToolBarProgress()
    }

    private fun initGlobalData() {
        displayLoadingData()
        viewModel.viewModelScope.launch {
            viewModel.fetchGlobalResults()?.collectLatest {
                globalAdapter.addOnPagesUpdatedListener {
                    onGlobalDataLoaded()
                }
                globalAdapter.submitData(it)
            }
        }
    }

    private fun displayLoadingData() {
        if (listAdapter.itemCount == 0) {
            initLoading(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
            )
        } else {
            displayResult(
                listOf(SearchResult(SearchResult.SearchResultType.LOADING)),
            )
        }
        showToolbarProgress()
    }

    private fun showToolbarProgress() {
        if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            if (context != null) {
                (context as SearchTEActivity).showProgress()
            } else {
                Timber.w("Cannot show toolbar progress - context is null")
            }
        }
    }

    private fun hideToolBarProgress() {
        if (context != null) {
            (context as SearchTEActivity).hideProgress()
        }
    }


    private fun initLoading(result: List<SearchResult>?) {
        recycler.post {
            initialLoadingAdapter.submitList(result)
        }
    }

    private fun displayResult(result: List<SearchResult>?) {
        recycler.post {
            resultAdapter.submitList(result)
        }
    }

    @ExperimentalAnimationApi
    private fun configureSequentialSearchNextAction(composeView: ComposeView) {
        composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val sequentialSearch by viewModel.sequentialSearch.observeAsState()

                updateLayoutParams<CoordinatorLayout.LayoutParams> {
                    val bottomMargin = if (viewModel.isBottomNavigationBarVisible()) {
                        56.dp
                    } else {
                        16.dp
                    }
                    setMargins(0, 0, 0, bottomMargin)
                }

                val notNewActions = sequentialSearch?.nextActions?.filterNot {
                    it == SequentialSearchAction.RegisterNew
                } ?: emptyList()

                if (notNewActions.isNotEmpty()) {
                    SequentialNextSearchActions(
                        sequentialSearchActions = sequentialSearch?.nextActions!!,
                        onClick = { action -> viewModel.sequentialSearchNextAction(action) })
                }
            }
        }
    }

    private fun onSearchTeiModelClick(item: SearchTeiModel) {
        currentLastClickedTeiUid = item.tei.uid()

        viewModel.onSearchTeiModelClick(item)
    }

    private fun configureBiometricsLoader(composeView: ComposeView) {
        composeView.apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                val sequentialSearch by viewModel.sequentialSearch.observeAsState()
                val biometricAppLaunching by viewModel.biometricAppLaunching.observeAsState(false)
                val isBiometricSearch = sequentialSearch is SequentialSearch.BiometricsSearch
                var isLoading by remember { mutableStateOf(false) }

                LaunchedEffect(isBiometricSearch) {
                    if (isBiometricSearch) {
                        liveAdapter.loadStateFlow.collect { loadState ->
                            isLoading = loadState.refresh is LoadState.Loading
                        }
                    } else {
                        isLoading = false
                    }
                }

                if (biometricAppLaunching || (isBiometricSearch && isLoading)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingContent(
                            loadingDescription = stringResource(org.dhis2.R.string.search_loading_more)
                        )
                    }
                }
            }
        }
    }

    private fun configureRecyclerVisibility() {
        viewModel.biometricAppLaunching.observe(viewLifecycleOwner) { isLaunching ->
            if (isLaunching) {
                recycler.visibility = View.GONE
            }
        }

        viewModel.sequentialSearch.observe(viewLifecycleOwner) { sequentialSearch ->
            val isBiometricSearch = sequentialSearch is SequentialSearch.BiometricsSearch

            if (isBiometricSearch) {
                recycler.visibility = View.GONE
            } else {
                lifecycleScope.launch {
                    delay(200)
                    recycler.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                liveAdapter.loadStateFlow.collect { loadState ->
                    val isBiometricSearch =
                        viewModel.sequentialSearch.value is SequentialSearch.BiometricsSearch
                    val biometricAppLaunching = viewModel.biometricAppLaunching.value == true

                    val isLoading = loadState.refresh is LoadState.Loading

                    if (isBiometricSearch) {
                        if (biometricAppLaunching || isLoading) {
                            recycler.visibility = View.GONE
                        } else {
                            recycler.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}
