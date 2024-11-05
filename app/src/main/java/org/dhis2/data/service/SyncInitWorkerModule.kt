package org.dhis2.data.service

import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerService
import org.dhis2.commons.prefs.BasicPreferenceProvider
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.biometrics.BiometricsConfigApi
import org.dhis2.data.biometrics.BiometricsConfigRepositoryImpl
import org.dhis2.data.notifications.NotificationD2Repository
import org.dhis2.data.notifications.NotificationsApi
import org.dhis2.data.notifications.UserGroupsApi
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.usescases.biometrics.repositories.BiometricsConfigRepository
import org.dhis2.usescases.notifications.domain.NotificationRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class SyncInitWorkerModule {
    @Provides
    @PerService
    fun syncRepository(d2: D2): SyncRepository {
        return SyncRepositoryImpl(d2)
    }

    @Provides
    @PerService
    fun biometricsConfigRepository(
        d2: D2,
        basicPreferences: BasicPreferenceProvider
    ): BiometricsConfigRepository {
        val biometricsConfigApi = d2.retrofit().create(
            BiometricsConfigApi::class.java
        )
        return BiometricsConfigRepositoryImpl(d2, basicPreferences, biometricsConfigApi)
    }

    @Provides
    @PerService
    fun notificationsRepository(
        d2: D2,
        preference: BasicPreferenceProvider
    ): NotificationRepository {
        val notificationsApi = d2.retrofit().create(
            NotificationsApi::class.java
        )
        val userGroupsApi = d2.retrofit().create(UserGroupsApi::class.java)
        return NotificationD2Repository(d2, preference, notificationsApi, userGroupsApi)
    }

    @Provides
    @PerService
    internal fun syncPresenter(
        d2: D2,
        preferences: PreferenceProvider,
        workManagerController: WorkManagerController,
        analyticsHelper: AnalyticsHelper,
        syncStatusController: SyncStatusController,
        syncRepository: SyncRepository,
        biometricsConfigRepository: BiometricsConfigRepository,
        notificationsRepository: NotificationRepository
    ): SyncPresenter {
        return SyncPresenterImpl(
            d2,
            preferences,
            workManagerController,
            analyticsHelper,
            syncStatusController,
            syncRepository,
            biometricsConfigRepository,
            notificationsRepository
        )
    }
}
