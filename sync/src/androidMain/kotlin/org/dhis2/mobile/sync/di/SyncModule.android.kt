package org.dhis2.mobile.sync.di

import androidx.work.WorkManager
import org.dhis2.mobile.sync.data.AndroidSyncBackgroundJobAction
import org.dhis2.mobile.sync.data.AndroidSyncRepository
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncDataWorker
import org.dhis2.mobile.sync.data.SyncMetadataWorker
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.data.SyncSettingsWorker
import org.dhis2.mobile.sync.domain.CheckPeriodicJobs
import org.dhis2.mobile.sync.domain.SyncData
import org.dhis2.mobile.sync.domain.SyncMetadata
import org.dhis2.mobile.sync.domain.SyncSettings
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val syncModule =
    module {

        singleOf(::SyncStatusController)

        factory {
            WorkManager.getInstance(get())
        }
        factory<SyncBackgroundJobAction> {
            AndroidSyncBackgroundJobAction(
                workManager = get(),
            )
        }

        factory<SyncRepository> {
            AndroidSyncRepository(get(), get(), get(), get(), get())
        }

        factory {
            // Do not replace with factoryOf(::SyncMetadata): factoryOf uses constructor
            // reflection and does not honour postMetadataSyncActions' default value, so a
            // flavor with no PostMetadataSyncModule would fail to resolve this factory
            // instead of falling back to an empty list.
            SyncMetadata(
                repository = get(),
                syncBackgroundJobAction = get(),
                // Downstream builds may register their own post-metadata-sync work.
                postMetadataSyncActions = getOrNull() ?: emptyList(),
            )
        }

        factoryOf(::SyncData)

        factoryOf(::SyncSettings)

        factoryOf(::CheckPeriodicJobs)

        workerOf(::SyncDataWorker)
        workerOf(::SyncMetadataWorker)
        workerOf(::SyncSettingsWorker)
    }
