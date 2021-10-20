package org.dhis2.usescases.biometrics.duplicates

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.Bindings.valueTypeHintMap
import org.dhis2.R
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.data.enrollment.EnrollmentUiDataHelper
import org.dhis2.data.filter.FilterPresenter
import org.dhis2.data.forms.dataentry.FormUiModelColorFactoryImpl
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.sorting.SearchSortingValueSetter
import org.dhis2.form.ui.style.FormUiColorFactory
import org.dhis2.usescases.searchTrackEntity.SearchRepository
import org.dhis2.usescases.searchTrackEntity.SearchRepositoryImpl
import org.dhis2.utils.DateUtils
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2

@Module
class BiometricsDuplicatesDialogModule(private val context:Context, private val teiType: String) {

    @Provides
    open fun provideFormUiColorFactory(): FormUiColorFactory {
        return FormUiModelColorFactoryImpl(context, false)
    }

    @Provides
    fun fieldViewModelFactory(
        context: Context,
        colorFactory: FormUiColorFactory
    ): FieldViewModelFactory {
        return FieldViewModelFactoryImpl(context.valueTypeHintMap(), true, colorFactory)
    }

    @Provides
    fun enrollmentUiDataHelper(context: Context): EnrollmentUiDataHelper {
        return EnrollmentUiDataHelper(context)
    }

    @Provides
    fun searchSortingValueSetter(
        context: Context,
        d2: D2,
        enrollmentUiDataHelper: EnrollmentUiDataHelper
    ): SearchSortingValueSetter {
        val unknownLabel = context.getString(R.string.unknownValue)
        val eventDateLabel = context.getString(R.string.most_recent_event_date)
        val enrollmentStatusLabel = context.getString(R.string.filters_title_enrollment_status)
        val enrollmentDateDefaultLabel = context.getString(R.string.enrollment_date)
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
    fun searchRepository(
        d2: D2,
        filterPresenter: FilterPresenter,
        resources: ResourceManager,
        searchSortingValueSetter: SearchSortingValueSetter,
        fieldFactory: FieldViewModelFactory,
        periodUtils: DhisPeriodUtils
    ): SearchRepository{
        return SearchRepositoryImpl(
            teiType,
            d2,
            filterPresenter,
            resources,
            searchSortingValueSetter,
            fieldFactory,
            periodUtils
        )
    }

    @Provides
    fun providesPresenter(
        d2: D2,
        searchRepository: SearchRepository,
        schedulerProvider: SchedulerProvider
    ): BiometricsDuplicatesDialogPresenter {
        return BiometricsDuplicatesDialogPresenter(d2, searchRepository, schedulerProvider)
    }
}