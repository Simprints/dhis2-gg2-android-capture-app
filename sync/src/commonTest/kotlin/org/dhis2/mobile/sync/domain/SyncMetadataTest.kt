package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.commons.domain.PostMetadataSyncAction
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.data.SyncRepository
import org.dhis2.mobile.sync.model.SMSConfigResult
import org.dhis2.mobile.sync.model.SyncPeriod
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncMetadataTest {
    private val repository: SyncRepository = mock()
    private val syncBackgroundJobAction: SyncBackgroundJobAction = mock()

    private val syncMetadata =
        SyncMetadata(
            repository,
            syncBackgroundJobAction,
        )

    @Test
    fun `Should not trigger background jobs if sync periods do not change`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction, never()).launchMetadataSync(any())
            verify(syncBackgroundJobAction, never()).cancelMetadataSync()
            verify(syncBackgroundJobAction, never()).launchDataSync(any())
            verify(syncBackgroundJobAction, never()).cancelDataSync()
        }

    @Test
    fun `Should cancel metadata sync if period changes to manual`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelMetadataSync()
            verify(syncBackgroundJobAction).launchSyncSettings()
        }

    @Test
    fun `Should re-launch metadata sync if period changes`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchMetadataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should cancel data sync if period changes to manual`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Manual,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).cancelDataSync()
        }

    @Test
    fun `Should re-launch data sync if period changes`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()).thenReturn(
                SyncPeriod.Every24Hour,
                SyncPeriod.Every7Days,
            )
            whenever(repository.syncMetadata(any())) doReturn Result.success(Unit)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            syncMetadata.invoke { }

            verify(syncBackgroundJobAction).launchDataSync(SyncPeriod.Every7Days.toSeconds())
        }

    @Test
    fun `Should return failure and save state when metadata sync fails`() =
        runBlocking {
            whenever(repository.isServerAvailable(any())) doReturn true
            val exception = Exception("Sync failed")
            whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
            whenever(repository.syncMetadata(any())) doReturn Result.failure(exception)
            whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
            whenever(repository.toggleSMS(true)) doReturn Result.success(Unit)

            val result = syncMetadata.invoke { }

            verify(repository).saveMetadataSyncState(false)
            assert(result.isFailure)
            assert(result.exceptionOrNull() == exception)
        }

    private suspend fun stubSync(syncResult: Result<Unit> = Result.success(Unit)) {
        whenever(repository.isServerAvailable(any())) doReturn true
        whenever(repository.currentMetadataSyncPeriod()) doReturn SyncPeriod.Manual
        whenever(repository.currentDataSyncPeriod()) doReturn SyncPeriod.Manual
        whenever(repository.syncMetadata(any())) doReturn syncResult
        whenever(repository.setUpSMS()) doReturn Result.success(SMSConfigResult.DoNothing)
    }

    private fun useCaseWith(vararg actions: PostMetadataSyncAction) =
        SyncMetadata(
            repository,
            syncBackgroundJobAction,
            actions.toList(),
        )

    @Test
    fun `Should run post metadata sync actions in order after a successful sync`() =
        runBlocking {
            stubSync()
            val executed = mutableListOf<String>()
            val useCase =
                useCaseWith(
                    PostMetadataSyncAction {
                        executed.add("first")
                        Result.success(Unit)
                    },
                    PostMetadataSyncAction {
                        executed.add("second")
                        Result.success(Unit)
                    },
                )

            val result = useCase.invoke { }

            assertTrue(result.isSuccess)
            assertEquals(listOf("first", "second"), executed)
        }

    @Test
    fun `Should not run post metadata sync actions when the sync fails`() =
        runBlocking {
            stubSync(Result.failure(Exception("boom")))
            var executed = false
            val useCase =
                useCaseWith(
                    PostMetadataSyncAction {
                        executed = true
                        Result.success(Unit)
                    },
                )

            val result = useCase.invoke { }

            assertTrue(result.isFailure)
            assertFalse(executed)
        }

    @Test
    fun `Should keep the sync successful when an action returns a failure`() =
        runBlocking {
            assertFailingActionIsIsolated(
                PostMetadataSyncAction { Result.failure(Exception("action failed")) },
            )
        }

    @Test
    fun `Should keep the sync successful when an action throws`() =
        runBlocking {
            assertFailingActionIsIsolated(
                PostMetadataSyncAction { error("action exploded") },
            )
        }

    /** A failing action must not fail the sync, nor stop the actions queued after it. */
    private suspend fun assertFailingActionIsIsolated(failingAction: PostMetadataSyncAction) {
        stubSync()
        var laterActionRan = false
        val useCase =
            useCaseWith(
                failingAction,
                PostMetadataSyncAction {
                    laterActionRan = true
                    Result.success(Unit)
                },
            )

        val result = useCase.invoke { }

        assertTrue(result.isSuccess)
        assertTrue(laterActionRan)
        verify(repository).saveMetadataSyncState(true)
    }
}
