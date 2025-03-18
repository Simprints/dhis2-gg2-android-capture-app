package org.dhis2.usescases.searchTrackEntity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment.Companion.forProgram
import io.reactivex.functions.Consumer
import org.dhis2.App
import org.dhis2.R
import org.dhis2.bindings.clipWithRoundedCorners
import org.dhis2.bindings.clipWithTopRightRoundedCorner
import org.dhis2.bindings.doOnItemSelected
import org.dhis2.bindings.dp
import org.dhis2.bindings.isKeyboardOpened
import org.dhis2.bindings.overrideHeight
import org.dhis2.commons.Constants
import org.dhis2.commons.biometrics.BIOMETRICS_CONFIRM_IDENTITY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_IDENTIFY_REQUEST
import org.dhis2.commons.biometrics.BIOMETRICS_USER_NOT_FOUND
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.date.DateUtils.OnNextSelected
import org.dhis2.commons.date.Period
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.FiltersAdapter
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.OnDismissListener
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.sync.SyncContext.TrackerProgramTei
import org.dhis2.data.biometrics.BiometricsClientFactory
import org.dhis2.data.biometrics.IdentifyResult
import org.dhis2.data.biometrics.IdentifyResult.BiometricsDeclined
import org.dhis2.data.biometrics.IdentifyResult.UserNotFound
import org.dhis2.data.biometrics.SimprintsItem
import org.dhis2.data.forms.dataentry.ProgramAdapter
import org.dhis2.databinding.ActivitySearchBinding
import org.dhis2.form.ui.intent.FormIntent.OnSave
import org.dhis2.tracker.NavigationBarUIState
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.biometrics.ui.SearchHelperFragment
import org.dhis2.usescases.biometrics.ui.SequentialSearchAction.SearchWithAttributes
import org.dhis2.usescases.biometrics.ui.SequentialSearchAction.SearchWithBiometrics
import org.dhis2.usescases.biometrics.ui.confirmationDialog.BIOMETRICS_SEARCH_CONFIRMATION_DIALOG_TAG
import org.dhis2.usescases.biometrics.ui.confirmationDialog.BiometricsSearchConfirmationDialog
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.searchTrackEntity.LegacyInteraction.OnAddRelationship
import org.dhis2.usescases.searchTrackEntity.LegacyInteraction.OnEnroll
import org.dhis2.usescases.searchTrackEntity.LegacyInteraction.OnEnrollClick
import org.dhis2.usescases.searchTrackEntity.LegacyInteraction.OnSearchTeiModelClick
import org.dhis2.usescases.searchTrackEntity.LegacyInteraction.OnSyncIconClick
import org.dhis2.usescases.searchTrackEntity.listView.SearchTEList.Companion.get
import org.dhis2.usescases.searchTrackEntity.mapView.SearchTEMap.Companion.get
import org.dhis2.usescases.searchTrackEntity.searchparameters.initSearchScreen
import org.dhis2.usescases.searchTrackEntity.ui.SearchScreenConfigurator
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.dhis2.utils.customviews.BreakTheGlassBottomDialog
import org.dhis2.utils.customviews.RxDateDialog
import org.dhis2.utils.customviews.navigationbar.NavigationPage
import org.dhis2.utils.granularsync.SyncStatusDialog
import org.dhis2.utils.granularsync.shouldLaunchSyncDialog
import org.dhis2.utils.isLandscape
import org.dhis2.utils.isPortrait
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.mobile.ui.designsystem.component.navigationBar.NavigationBar
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import timber.log.Timber
import java.io.Serializable
import java.util.Date
import javax.inject.Inject

class SearchTEActivity : ActivityGlobalAbstract(), SearchTEContractsModule.View {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchScreenConfigurator: SearchScreenConfigurator

    @Inject
    lateinit var presenter: SearchTEContractsModule.Presenter

    @Inject
    lateinit var filtersAdapter: FiltersAdapter

    @Inject
    lateinit var viewModelFactory: SearchTeiViewModelFactory

    @Inject
    lateinit var searchNavigator: SearchNavigator

    @Inject
    lateinit var networkUtils: NetworkUtils

    @Inject
    lateinit var themeManager: ThemeManager

    @Inject
    lateinit var featureConfig: FeatureConfigRepository

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var teiCardMapper: TEICardMapper

    private var initialProgram: String? = null
    private var initialQuery: Map<String, String>? = null

    private var fromRelationship = false
    private var fromRelationshipTeiUid: String? = null
    private var fromAnalytics = false

    private lateinit var tEType: String

    private val viewModel: SearchTEIViewModel by viewModels<SearchTEIViewModel> { viewModelFactory }

    private var initSearchNeeded = true
    var searchComponent: SearchTEComponent? = null

    enum class Extra(val key: String) {
        TEI_UID("TRACKED_ENTITY_UID"),
        PROGRAM_UID("PROGRAM_UID"),
        QUERY_ATTR("QUERY_DATA_ATTR"),
        QUERY_VALUES("QUERY_DATA_VALUES"),
        ;

        fun key(): String {
            return key
        }
    }

    private enum class Content {
        LIST,
        MAP,
        ANALYTICS,
    }

    private var currentContent: Content? = null

    private var searchHelperFragment: SearchHelperFragment? = null
    private var biometricsErrorDialog: CustomDialog? = null
    private var lastSelection: SearchTeiModel? = null

    @OptIn(ExperimentalAnimationApi::class)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        initializeVariables(savedInstanceState)
        inject()

        if (initialProgram != null) {
            themeManager.setProgramTheme(initialProgram!!)
        }
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_search)
        val currentScreen = savedInstanceState?.getString(CURRENT_SCREEN).orEmpty()
        if (currentScreen.isNotBlank()) {
            currentContent = Content.valueOf(currentScreen)
        }
        initSearchParameters()

        searchScreenConfigurator = SearchScreenConfigurator(
            binding,
        ) { isOpen: Boolean ->
            viewModel.setFiltersOpened(isOpen)
        }

        binding.setPresenter(presenter)
        binding.setTotalFilters(FilterManager.getInstance().totalFilters)

        if (isLandscape()) {
            viewModel.filtersOpened.observe(this) { isOpened: Boolean ->
                if (java.lang.Boolean.TRUE == isOpened) {
                    binding.mainComponent.clipWithRoundedCorners(16.dp)
                } else {
                    binding.mainComponent.clipWithTopRightRoundedCorner(16.dp)
                }
            }
        } else {
            binding.mainComponent.clipWithRoundedCorners(16.dp)
        }

        binding.filterRecyclerLayout.adapter = filtersAdapter

        binding.executePendingBindings()

        binding.syncButton.visibility = if (initialProgram != null) View.VISIBLE else View.GONE
        binding.syncButton.setOnClickListener { openSyncDialog() }

        binding.landOpenSearchButton
            .setLandscapeOpenSearchButton(
                viewModel,
            ) {
                viewModel.setSearchScreen()
            }

        setupBottomNavigation()
        observeScreenState()
        observeDownload()
        observeLegacyInteractions()

        if (intent.shouldLaunchSyncDialog()) {
            openSyncDialog()
        }

        initSearchHelperFragment()

        viewModel.setOnSequentialSearchActionListener { action ->
            if (action != null) {
                if (action is SearchWithBiometrics) {
                    presenter.onBiometricsClick()
                } else if (action is SearchWithAttributes) {
                    viewModel.openSearchForm()
                } else {
                    presenter.onEnrollClick(
                        java.util.HashMap(viewModel.queryData),
                        viewModel.sequentialSearch.value
                    )

                    viewModel.resetSequentialSearch()
                    viewModel.clearQueryData()
                }
            }
            Unit
        }
    }

    private fun initializeVariables(savedInstanceState: Bundle?) {
        tEType = intent.getStringExtra("TRACKED_ENTITY_UID").orEmpty()
        initialProgram = intent.getStringExtra("PROGRAM_UID")
        try {
            fromRelationship = intent.getBooleanExtra("FROM_RELATIONSHIP", false)
            fromRelationshipTeiUid = intent.getStringExtra("FROM_RELATIONSHIP_TEI")
        } catch (e: Exception) {
            Timber.d(e)
        }
        initialQuery = this.queryDataExtra(savedInstanceState)
    }

    private fun inject() {
        searchComponent =
            (applicationContext as App).userComponent()?.plus(
                SearchTEModule(
                    this,
                    tEType,
                    initialProgram,
                    context,
                    initialQuery,
                ),
            )
        searchComponent?.inject(this)
    }

    override fun onResume() {
        super.onResume()
        if (sessionManagerServiceImpl.isUserLoggedIn()) {
            FilterManager.getInstance().clearUnsupportedFilters()
            if (initSearchNeeded) {
                presenter.init()
            } else {
                initSearchNeeded = true
            }
            binding.totalFilters = FilterManager.getInstance().totalFilters
        }
    }

    override fun onPause() {
        if (sessionManagerServiceImpl.isUserLoggedIn()) {
            presenter.setOpeningFilterToNone()
            if (initSearchNeeded) {
                presenter.onDestroy()
            }
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (sessionManagerServiceImpl.isUserLoggedIn()) {
            presenter.onDestroy()
            FilterManager.getInstance().clearEnrollmentStatus()
            FilterManager.getInstance().clearEventStatus()
            FilterManager.getInstance().clearEnrollmentDate()
            FilterManager.getInstance().clearWorkingList(true)
            FilterManager.getInstance().clearSorting()
            FilterManager.getInstance().clearAssignToMe()
            FilterManager.getInstance().clearFollowUp()
            presenter.clearOtherFiltersIfWebAppIsConfig()
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed(
            isPortrait(),
            viewModel.backdropActive.value ?: false,
            this.isKeyboardOpened(),
            {
                super.onBackPressed()
            },
            {
                if (viewModel.filterIsOpen()) {
                    showHideFilterGeneral()
                }
                viewModel.setPreviousScreen()
            },
            {
                hideKeyboard()
            },
        )
    }

    override fun onBackClicked() {
        onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(Constants.QUERY_DATA, viewModel.queryData as Serializable)
        outState.putString(CURRENT_SCREEN, currentContent?.name)
    }

    private fun openSyncDialog() {
        val contextView = findViewById<View>(R.id.navigationBar)
        SyncStatusDialog.Builder()
            .withContext(this, null)
            .withSyncContext(
                SyncContext.TrackerProgram(initialProgram!!),
            )
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) viewModel.refreshData()
                }
            })
            .onNoConnectionListener {
                Snackbar.make(
                    contextView,
                    R.string.sync_offline_check_connection,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }
            .show("PROGRAM_SYNC")
    }

    override fun updateFilters(totalFilters: Int) {
        binding.totalFilters = totalFilters
        binding.executePendingBindings()
        viewModel.updateActiveFilters(totalFilters > 0)
        viewModel.refreshData()
    }

    private fun initSearchParameters() {
        initSearchScreen(
            binding.searchContainer,
            viewModel,
            initialProgram,
            tEType,
            resourceManager,
            { uid: String, preselectedOrgUnits: List<String>, orgUnitScope: OrgUnitSelectorScope, label: String ->
                OUTreeFragment.Builder()
                    .withPreselectedOrgUnits(preselectedOrgUnits)
                    .singleSelection()
                    .onSelection { selectedOrgUnits: List<OrganisationUnit> ->
                        var selectedOrgUnit: String? = null
                        if (selectedOrgUnits.isNotEmpty()) {
                            selectedOrgUnit = selectedOrgUnits[0].uid()
                        }
                        viewModel.onParameterIntent(
                            OnSave(
                                uid,
                                selectedOrgUnit,
                                ValueType.ORGANISATION_UNIT,
                                null,
                                true,
                            ),
                        )
                    }
                    .orgUnitScope(orgUnitScope)
                    .build()
                    .show(supportFragmentManager, label)
            },
            {
                binding.root.closeKeyboard()
                presenter.onClearClick()
            },
        )
    }

    private fun setupBottomNavigation() {
        binding.navigationBar.setContent {
            DHIS2Theme {
                val uiState by viewModel.navigationBarUIState
                val isBackdropActive by viewModel.backdropActive.observeAsState(false)
                var selectedItemIndex by remember(uiState) {
                    mutableIntStateOf(
                        uiState.items.indexOfFirst {
                            it.id == uiState.selectedItem
                        },
                    )
                }

                LaunchedEffect(uiState.selectedItem) {
                    handleBottomNavigation(
                        uiState = uiState,
                        onDialogDismissed = {
                            selectedItemIndex = 0
                        },
                    )
                }

                AnimatedVisibility(
                    visible = (isBackdropActive.not() && uiState.items.isNotEmpty()) || isLandscape(),
                    enter = slideInVertically(animationSpec = tween(200)) { it },
                    exit = slideOutVertically(animationSpec = tween(200)) { it },
                ) {
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        items = uiState.items,
                        selectedItemIndex = selectedItemIndex,
                    ) { page ->
                        selectedItemIndex = uiState.items.indexOfFirst { it.id == page }
                        if (sessionManagerServiceImpl.isUserLoggedIn().not()) return@NavigationBar

                        if (viewModel.backdropActive.value == true) {
                            searchScreenConfigurator.closeBackdrop()
                        }

                        viewModel.onNavigationPageChanged(page)
                    }
                }
            }
        }
    }

    private fun handleBottomNavigation(
        uiState: NavigationBarUIState<NavigationPage>,
        onDialogDismissed: () -> Unit,
    ) {
        when (uiState.selectedItem) {
            NavigationPage.LIST_VIEW -> {
                viewModel.setListScreen()
                showList()
                showSearchAndFilterButtons()
            }

            NavigationPage.MAP_VIEW -> {
                networkUtils.performIfOnline(
                    context = this@SearchTEActivity,
                    action = {
                        presenter.trackSearchMapVisualization()
                        showMap()
                        showSearchAndFilterButtons()
                    },
                    onDialogDismissed = onDialogDismissed,
                    noNetworkMessage = getString(R.string.msg_network_connection_maps),
                )
            }

            NavigationPage.ANALYTICS -> {
                if (sessionManagerServiceImpl.isUserLoggedIn()) {
                    presenter.trackSearchAnalytics()
                    viewModel.setAnalyticsScreen()
                    fromAnalytics = true
                    showAnalytics()
                    hideSearchAndFilterButtons()
                }
            }

            else -> {
                // no-op
            }
        }
    }

    private fun showList() {
        if (currentContent != Content.LIST) {
            currentContent = Content.LIST
            supportFragmentManager.beginTransaction().run {
                replace(R.id.mainComponent, get(fromRelationship))
                commit()
            }
            hideToolbarProgressBar()
        }
        viewModel.refreshData.observe(this) {
            binding.root.closeKeyboard()
        }
    }

    private fun showMap() {
        if (currentContent != Content.MAP) {
            currentContent = Content.MAP
            supportFragmentManager.beginTransaction().run {
                replace(R.id.mainComponent, get(fromRelationship, tEType))
                commit()
            }
            observeMapLoading()
        }
    }

    private fun showAnalytics() {
        if (currentContent != Content.ANALYTICS) {
            currentContent = Content.ANALYTICS
            supportFragmentManager.beginTransaction().run {
                replace(
                    R.id.mainComponent,
                    forProgram(
                        initialProgram!!,
                    ),
                )
                commit()
            }
            hideToolbarProgressBar()
        }
    }

    private fun hideToolbarProgressBar() {
        if (binding.toolbarProgress.isShown) {
            binding.toolbarProgress.hide()
        }
    }

    private fun hideSearchAndFilterButtons() {
        binding.searchFilterGeneral.visibility = View.GONE
        binding.filterCounter.visibility = View.GONE
    }

    private fun showSearchAndFilterButtons() {
        if (fromAnalytics) {
            fromAnalytics = false
            binding.searchFilterGeneral.visibility = View.VISIBLE
            binding.filterCounter.visibility =
                if ((binding.totalFilters ?: 0) > 0) View.VISIBLE else View.GONE
        }
    }

    private fun observeScreenState() {
        viewModel.screenState.observe(this, searchScreenConfigurator::configure)
        viewModel.screenState.observe(this, viewModel::updateBackdrop)
    }

    private fun observeDownload() {
        viewModel.downloadResult.observe(this) { result: TeiDownloadResult ->
            result.handleResult(
                { teiUid: String, programUid: String?, enrollmentUid: String? ->
                    openDashboard(
                        teiUid,
                        programUid,
                        enrollmentUid,
                        presenter.getLastBiometricsSessionId()
                    )
                },
                { teiUid: String, enrollmentUid: String? ->
                    showBreakTheGlass(teiUid, enrollmentUid)
                },
                {
                    couldNotDownload(presenter.trackedEntityName.displayName())
                },
                { errorMessage: String? ->
                    displayMessage(errorMessage)
                },
            )
        }
    }

    private fun observeLegacyInteractions() {
        viewModel.legacyInteraction.observe(this) { legacyInteraction ->
            if (legacyInteraction != null) {
                when (legacyInteraction.id) {
                    LegacyInteractionID.ON_ENROLL_CLICK -> {
                        val interaction = legacyInteraction as OnEnrollClick
                        presenter.onEnrollClick(HashMap(interaction.queryData),
                            viewModel.sequentialSearch.value)
                    }

                    LegacyInteractionID.ON_ADD_RELATIONSHIP -> {
                        val interaction = legacyInteraction as OnAddRelationship
                        presenter.addRelationship(
                            interaction.teiUid,
                            interaction.relationshipTypeUid,
                            interaction.online,
                        )
                    }

                    LegacyInteractionID.ON_SYNC_CLICK -> {
                        val interaction = legacyInteraction as OnSyncIconClick
                        presenter.onSyncIconClick(interaction.teiUid)
                    }

                    LegacyInteractionID.ON_ENROLL -> {
                        val interaction = legacyInteraction as OnEnroll
                        presenter.enroll(
                            interaction.initialProgramUid,
                            interaction.teiUid,
                            HashMap(interaction.queryData),
                        )
                    }

                    LegacyInteractionID.ON_TEI_CLICK -> {
                        // Eyeseetea customization, we use ON_SEARCH_TEI_MODEL_CLICK
                        /*val interaction = legacyInteraction as OnTeiClick
                        presenter.onTEIClick(
                            interaction.teiUid,
                            interaction.enrollmentUid,
                            interaction.online,
                        )*/
                    }
                    LegacyInteractionID.ON_SEARCH_TEI_MODEL_CLICK -> {
                        val interaction = legacyInteraction as OnSearchTeiModelClick
                        presenter.onSearchTEIModelClick(
                            interaction.item,
                            viewModel.sequentialSearch.value
                        )
                    }
                }
                viewModel.onLegacyInteractionConsumed()
            }
        }
    }

    private fun observeMapLoading() {
        viewModel.refreshData.observe(this) {
            if (currentContent == Content.MAP) {
                binding.toolbarProgress.show()
            }
        }
    }

    fun showProgress() {
        binding.toolbarProgress.show()
    }

    fun hideProgress() {
        binding.toolbarProgress.hide()
    }

    override fun clearList(uid: String?) {
        this.initialProgram = uid
        if (uid == null) binding.programSpinner.setSelection(0)
    }

    override fun setPrograms(programs: List<ProgramSpinnerModel>) {
        binding.programSpinner.adapter = ProgramAdapter(
            this,
            R.layout.spinner_program_layout,
            R.id.spinner_text,
            programs,
            presenter.trackedEntityName.displayName(),
        )
        if (initialProgram != null && initialProgram!!.isNotEmpty()) {
            setInitialProgram(programs)
        } else {
            binding.programSpinner.setSelection(0)
        }

        binding.programSpinner.overrideHeight(500)
        binding.programSpinner.doOnItemSelected { selectedIndex: Int ->
            viewModel.onProgramSelected(selectedIndex, programs) { selectedProgram: String? ->
                changeProgram(selectedProgram)
            }
        }
    }

    override fun showSyncDialog(enrollmentUid: String) {
        val contextView = findViewById<View>(R.id.navigationBar)
        SyncStatusDialog.Builder()
            .withContext(this, null)
            .withSyncContext(
                TrackerProgramTei(enrollmentUid),
            )
            .onDismissListener(object : OnDismissListener {
                override fun onDismiss(hasChanged: Boolean) {
                    if (hasChanged) viewModel.refreshData()
                }
            })
            .onNoConnectionListener {
                Snackbar.make(
                    contextView,
                    R.string.sync_offline_check_connection,
                    Snackbar.LENGTH_SHORT,
                ).show()
            }.show("TEI_SYNC")
    }

    private fun setInitialProgram(programs: List<ProgramSpinnerModel>) {
        for (i in programs.indices) {
            if (programs[i].uid == initialProgram) {
                binding.programSpinner.setSelection(i + 1)
            }
        }
    }

    private fun changeProgram(programUid: String?) {
        searchNavigator.changeProgram(
            programUid,
            viewModel.queryDataByProgram(programUid),
            fromRelationshipTeiUid,
        )
    }

    override fun fromRelationshipTEI(): String? {
        return fromRelationshipTeiUid
    }

    override fun showHideFilterGeneral() {
        viewModel.onFiltersClick(isLandscape())
    }

    override fun setInitialFilters(filtersToDisplay: List<FilterItem>) {
        filtersAdapter.submitList(filtersToDisplay)
    }

    override fun hideFilter() {
        binding.searchFilterGeneral.visibility = View.GONE
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun clearFilters() {
        if (viewModel.filterIsOpen()) {
            filtersAdapter.notifyDataSetChanged()
            FilterManager.getInstance().clearAllFilters()
        }
    }

    override fun openOrgUnitTreeSelector() {
        OUTreeFragment.Builder()
            .withPreselectedOrgUnits(
                FilterManager.getInstance().orgUnitUidsFilters,
            )
            .onSelection { selectedOrgUnits: List<OrganisationUnit?>? ->
                presenter.setOrgUnitFilters(
                    selectedOrgUnits,
                )
            }
            .build()
            .show(supportFragmentManager, "OUTreeFragment")
    }

    override fun showPeriodRequest(periodRequest: Pair<PeriodRequest, Filters>) {
        if (periodRequest.first == PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(this) { datePeriod: List<DatePeriod?>? ->
                if (periodRequest.second == Filters.PERIOD) {
                    FilterManager.getInstance().addPeriod(datePeriod)
                } else {
                    FilterManager.getInstance().addEnrollmentPeriod(datePeriod)
                }
            }
        } else {
            val onFromToSelector = DateUtils.OnFromToSelector { datePeriods: List<DatePeriod?>? ->
                if (periodRequest.second == Filters.PERIOD) {
                    FilterManager.getInstance().addPeriod(datePeriods)
                } else {
                    FilterManager.getInstance().addEnrollmentPeriod(datePeriods)
                }
            }

            val onNextSelected = OnNextSelected {
                RxDateDialog(this, Period.WEEKLY)
                    .createForFilter().show()
                    .subscribe(
                        { selectedDates: org.dhis2.commons.data.tuples.Pair<Period?, List<Date?>?> ->
                            onFromToSelector.onFromToSelected(
                                DateUtils.getInstance().getDatePeriodListFor(
                                    selectedDates.val1(),
                                    selectedDates.val0(),
                                ),
                            )
                        },
                        { t: Throwable? -> Timber.e(t) },
                    )
            }

            DateUtils.getInstance().showPeriodDialog(
                this,
                onFromToSelector,
                true,
                onNextSelected,
            )
        }
    }

    override fun openDashboard(teiUid: String, programUid: String?, enrollmentUid: String?, sessionId: String) {
        searchNavigator.openDashboard(teiUid, programUid, enrollmentUid, sessionId)
        viewModel.resetSequentialSearch()
        viewModel.clearQueryData()
    }

    fun refreshData() {
        viewModel.refreshData()
    }

    override fun couldNotDownload(typeName: String?) {
        displayMessage(getString(R.string.download_tei_error, typeName))
    }

    override fun showBreakTheGlass(teiUid: String, enrollmentUid: String?) {
        BreakTheGlassBottomDialog()
            .setProgram(presenter.program.uid())
            .setPositiveButton { reason: String? ->
                viewModel.onDownloadTei(teiUid, enrollmentUid, reason)
            }
            .show(supportFragmentManager, BreakTheGlassBottomDialog::class.java.name)
    }

    override fun goToEnrollment(enrollmentUid: String, programUid: String) {
        searchNavigator.goToEnrollment(enrollmentUid, programUid, fromRelationshipTEI())
    }

    override fun downloadProgress(): Consumer<D2Progress> {
        return Consumer {
            Snackbar.make(
                binding.getRoot(),
                getString(R.string.downloading),
                BaseTransientBottomBar.LENGTH_SHORT,
            ).show()
        }
    }

    override fun launchBiometricsIdentify(moduleId: String?, userOrgUnits: List<String>) {
        BiometricsClientFactory.get(this).identify(this, moduleId, userOrgUnits)
    }

    override fun showBiometricsSearchConfirmation(item: SearchTeiModel) {
        lastSelection = item

        val dialog = BiometricsSearchConfirmationDialog(
            item,
            teiCardMapper,
            {
                lastSelection = null
                Unit
            },
            {
                viewModel.resetSequentialSearch()
                presenter.sendBiometricsConfirmIdentity(
                    lastSelection!!.tei.uid(),
                    lastSelection!!.selectedEnrollment.uid(), lastSelection!!.isOnline
                )
                Unit
            }
        )

        dialog.show(supportFragmentManager, BIOMETRICS_SEARCH_CONFIRMATION_DIALOG_TAG)
    }

    override fun sendBiometricsConfirmIdentity(
        sessionId: String, guid: String, teiUid: String,
        enrollmentUid: String, isOnline: Boolean
    ) {
        if (lastSelection != null) {
            BiometricsClientFactory.get(this).confirmIdentify(
                this,
                sessionId, guid, lastSelection!!.tei.uid()
            )
            viewModel.clearQueryData()
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            BIOMETRICS_IDENTIFY_REQUEST -> {
                val result = BiometricsClientFactory.get(
                    this
                ).handleIdentifyResponse(resultCode, data)

                if (result is IdentifyResult.Completed) {
                    val completedResult = result

                    presenter.searchOnBiometrics(
                        completedResult.items,
                        completedResult.sessionId, false
                    )
                } else if (result is BiometricsDeclined) {
                    Toast.makeText(
                        context, R.string.biometrics_declined,
                        Toast.LENGTH_SHORT
                    ).show()

                    simulateNotFoundBiometricsSearch(null)

                    launchSearchFormIfRequired()
                } else if (result is UserNotFound) {
                    Toast.makeText(
                        context, R.string.biometrics_user_not_found,
                        Toast.LENGTH_SHORT
                    ).show()

                    simulateNotFoundBiometricsSearch(result.sessionId)
                } else if (result is IdentifyResult.Failure) {
                    Toast.makeText(
                        context, R.string.biometrics_failed,
                        Toast.LENGTH_SHORT
                    ).show()

                    simulateNotFoundBiometricsSearch(null)

                    launchSearchFormIfRequired()
                } else if (result is IdentifyResult.AgeGroupNotSupported) {
                    Toast.makeText(
                        context, R.string.age_group_not_supported,
                        Toast.LENGTH_SHORT
                    ).show()

                    simulateNotFoundBiometricsSearch(null)
                }
            }

            BIOMETRICS_CONFIRM_IDENTITY_REQUEST -> {
                if (lastSelection != null) {
                    presenter.onSearchTEIModelClick(lastSelection, viewModel.sequentialSearch.value)
                    lastSelection = null
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun simulateNotFoundBiometricsSearch(sessionId: String?) {
        presenter.searchOnBiometrics(
            listOf<SimprintsItem>(SimprintsItem(BIOMETRICS_USER_NOT_FOUND, 0f)),
            sessionId, false
        )
    }

    private fun launchSearchFormIfRequired() {
        if (viewModel.sequentialSearch.value == null ||
            viewModel.sequentialSearch.value!!.nextActions.isEmpty()
        ) {
            Handler(Looper.getMainLooper()).postDelayed({ viewModel.openSearchForm() }, 100)
        }
    }

    private fun initSearchHelperFragment() {
        searchHelperFragment = SearchHelperFragment()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.searchHelperViewContainer, searchHelperFragment!!).commit()
    }

    companion object {
        private const val CURRENT_SCREEN = "current_screen"

        fun getIntent(
            context: Context?,
            programUid: String?,
            teiTypeToAdd: String?,
            teiUid: String?,
            fromRelationship: Boolean,
        ): Intent {
            val intent = Intent(context, SearchTEActivity::class.java)
            val extras = Bundle()
            extras.putBoolean("FROM_RELATIONSHIP", fromRelationship)
            extras.putString("FROM_RELATIONSHIP_TEI", teiUid)
            extras.putString(Extra.TEI_UID.key, teiTypeToAdd)
            extras.putString(Extra.PROGRAM_UID.key, programUid)
            intent.putExtras(extras)
            return intent
        }
    }
}
