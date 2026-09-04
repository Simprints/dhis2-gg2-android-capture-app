package org.dhis2.usescases.biometrics.duplicates

import android.content.Context
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.R
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.date.DateUtils
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.mobile.commons.network.NetworkStatusProvider
import org.dhis2.mobile.commons.network.NetworkStatusProviderImpl
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.PreferenceProviderImpl
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisEnrollmentUtils
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.dhis2.data.forms.dataentry.SearchTEIRepository
import org.dhis2.data.forms.dataentry.SearchTEIRepositoryImpl
import org.dhis2.data.sorting.SearchSortingValueSetter
import org.dhis2.form.data.metadata.FileResourceConfiguration
import org.dhis2.form.data.metadata.OptionSetConfiguration
import org.dhis2.form.data.metadata.OrgUnitConfiguration
import org.dhis2.form.ui.FieldViewModelFactory
import org.dhis2.form.ui.FieldViewModelFactoryImpl
import org.dhis2.form.ui.provider.AutoCompleteProviderImpl
import org.dhis2.form.ui.provider.DisplayNameProviderImpl
import org.dhis2.form.ui.provider.HintProviderImpl
import org.dhis2.form.ui.provider.KeyboardActionProviderImpl
import org.dhis2.form.ui.provider.LegendValueProviderImpl
import org.dhis2.form.ui.provider.UiEventTypesProviderImpl
import org.dhis2.mobile.commons.customintents.CustomIntentRepository
import org.dhis2.mobile.commons.customintents.CustomIntentRepositoryImpl
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.tracker.data.ProfilePictureProvider
import org.dhis2.tracker.search.data.SearchTrackedEntityRepository
import org.dhis2.tracker.search.data.SearchTrackedEntityRepositoryImpl
import org.dhis2.tracker.search.domain.SearchTrackedEntities
import org.dhis2.ui.ThemeManager
import org.dhis2.usescases.events.EventInfoProvider
import org.dhis2.usescases.searchTrackEntity.SearchRepository
import org.dhis2.usescases.searchTrackEntity.SearchRepositoryImpl
import org.dhis2.usescases.searchTrackEntity.SearchRepositoryImplKt
import org.dhis2.usescases.searchTrackEntity.SearchRepositoryKt
import org.dhis2.usescases.searchTrackEntity.ui.mapper.TEICardMapper
import org.dhis2.usescases.tracker.TrackedEntityInstanceInfoProvider
import org.hisp.dhis.android.core.D2

@Module
class BiometricsDuplicatesDialogModule(
    private val context: Context, private val teiType: String,
    private val initialProgram: String
) {

    @Provides
    fun enrollmentUiDataHelper(context: Context): EnrollmentUiDataHelper {
        return EnrollmentUiDataHelper(context)
    }

    @Provides
    fun searchSortingValueSetter(
        context: Context,
        d2: D2,
        enrollmentUiDataHelper: EnrollmentUiDataHelper,
        resourceManager: ResourceManager,
    ): SearchSortingValueSetter {
        val unknownLabel = context.getString(R.string.unknownValue)
        val eventDateLabel = context.getString(R.string.most_recent_event_date)
        val enrollmentStatusLabel = resourceManager.formatWithEnrollmentLabel(
            initialProgram,
            R.string.filters_title_enrollment_status,
            1,
            false,
        )
        val enrollmentDateDefaultLabel = resourceManager.formatWithEnrollmentLabel(
            initialProgram,
            R.string.enrollment_date_V2,
            1,
            false,
        )
        val uiDateFormat = DateUtils.SIMPLE_DATE_FORMAT
        return SearchSortingValueSetter(
            d2,
            unknownLabel,
            eventDateLabel,
            enrollmentStatusLabel,
            enrollmentDateDefaultLabel,
            uiDateFormat,
            enrollmentUiDataHelper
        )
    }

    @Provides
    internal fun searchRepository(
        d2: D2,
        crashReportController: CrashReportController
    ): SearchTEIRepository {
        return SearchTEIRepositoryImpl(d2, DhisEnrollmentUtils(d2), crashReportController)
    }

    @Provides
    fun searchRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        resources: ResourceManager,
        charts: Charts,
        crashReportController: CrashReportController,
        networkStatusProvider: NetworkStatusProvider,
        searchTEIRepository: SearchTEIRepository,
        themeManager: ThemeManager,
        dateUtils: DateUtils,
        customIntentRepository: CustomIntentRepository,
        dispatcherProvider: DispatcherProvider,
        basicPreferenceProvider: BasicPreferenceProvider,
    ): SearchRepository =
        SearchRepositoryImpl(
            teiType,
            initialProgram,
            d2,
            filterPresenter,
            resources,
            charts,
            crashReportController,
            networkStatusProvider,
            searchTEIRepository,
            themeManager,
            dateUtils,
            customIntentRepository,
            dispatcherProvider,
            basicPreferenceProvider,
        )

    @Provides
    fun fieldViewModelFactory(
        context: Context,
        d2: D2,
        resourceManager: ResourceManager,
        colorUtils: ColorUtils,
        periodUtils: DhisPeriodUtils
    ): FieldViewModelFactory {
        return FieldViewModelFactoryImpl(
            HintProviderImpl(context),
            DisplayNameProviderImpl(
                OptionSetConfiguration(d2),
                OrgUnitConfiguration(d2),
                FileResourceConfiguration(d2),
                periodUtils
            ),
            UiEventTypesProviderImpl(),
            KeyboardActionProviderImpl(),
            LegendValueProviderImpl(d2, resourceManager),
            AutoCompleteProviderImpl(PreferenceProviderImpl(context))
        )
    }

    @Provides
    fun searchRepositoryKt(
        searchRepository: SearchRepository,
        d2: D2,
        dispatcherProvider: DispatcherProvider,
        metadataIconProvider: MetadataIconProvider,
        colorUtils: ColorUtils,
        dateUtils: DateUtils,
        customIntentRepository: CustomIntentRepository,
        sortingValueSetter: SearchSortingValueSetter,
    ): SearchRepositoryKt {
        val resourceManager = ResourceManager(context, colorUtils)
        val dateLabelProvider =
            DateLabelProvider(context, ResourceManager(context, colorUtils))
        val profilePictureProvider = ProfilePictureProvider(d2)

        return SearchRepositoryImplKt(
            searchRepository,
            d2,
            dispatcherProvider,
            TrackedEntityInstanceInfoProvider(
                d2,
                profilePictureProvider,
                dateLabelProvider,
                metadataIconProvider,
                sortingValueSetter,
            ),
            EventInfoProvider(
                d2,
                resourceManager,
                dateLabelProvider,
                metadataIconProvider,
                profilePictureProvider,
                dateUtils
            ),
            customIntentRepository
        )
    }

    @Provides
    fun providesPresenter(
        d2: D2,
        searchRepository: SearchRepository,
        searchRepositoryKt: SearchRepositoryKt,
        schedulerProvider: SchedulerProvider,
        basicPreferenceProvider: BasicPreferenceProvider,
        searchTrackedEntities: SearchTrackedEntities,
        dispatcherProvider: DispatcherProvider,
    ): BiometricsDuplicatesDialogPresenter {
        return BiometricsDuplicatesDialogPresenter(
            d2,
            searchRepository,
            searchRepositoryKt,
            schedulerProvider,
            basicPreferenceProvider,
            searchTrackedEntities,
            dispatcherProvider,
        )
    }

    // EyeSeeTea customization - Biometric Duplicate Review And Confirm Identity
    // Mirrors SearchTEModule: the duplicates dialog runs the same search, so it needs the same
    // use case and its repository.
    @Provides
    fun provideLoadSearchResultsUseCase(
        searchTrackedEntityRepository: SearchTrackedEntityRepository,
        customIntentRepository: CustomIntentRepository,
    ): SearchTrackedEntities = SearchTrackedEntities(
        searchTrackedEntityRepository,
        customIntentRepository,
        teiType,
    )

    @Provides
    fun provideLoadSearchResultsRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
    ): SearchTrackedEntityRepository = SearchTrackedEntityRepositoryImpl(
        d2,
        filterPresenter,
        ProfilePictureProvider(d2),
    )

    @Provides
    fun provideListCardMapper(
        context: Context,
        resourceManager: ResourceManager
    ): TEICardMapper {
        return TEICardMapper(context, resourceManager)
    }

    @Provides
    fun provideDateUtils(
    ): DateUtils {
        return DateUtils.getInstance()
    }

    @Provides
    fun provideCustomIntentRepository(d2: D2): CustomIntentRepository {
        return CustomIntentRepositoryImpl(d2)
    }

    @Provides
    fun provideNetworkStatusProvider(): NetworkStatusProvider = NetworkStatusProviderImpl(context)
}
