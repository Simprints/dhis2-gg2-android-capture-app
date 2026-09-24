package org.dhis2.usescases.main.domain

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.dhis2.data.service.VersionRepository
import org.dhis2.mobile.commons.error.DomainError
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.willAnswer

/**
 * DownloadNewVersion has a different implementation per flavor: some flavors call
 * [VersionRepository.download] (returns [org.dhis2.usescases.main.domain.model.DownloadMethod.File]),
 * others call [VersionRepository.getUrl] (returns [org.dhis2.usescases.main.domain.model.DownloadMethod.Url]).
 * Both repository methods are stubbed here so this single test source, shared by every
 * flavor, verifies the success/failure contract regardless of which one the active flavor uses.
 */
class DownloadNewVersionTest {
    private val versionRepository: VersionRepository = mock()
    private lateinit var downloadNewVersion: DownloadNewVersion

    @Before
    fun setUp() {
        downloadNewVersion = DownloadNewVersion(versionRepository)
    }

    @Test
    fun `should successfully resolve a new version`() =
        runTest {
            val fakeUriPath = "fakeUriPath"
            whenever(
                versionRepository.download(
                    context = any(),
                    onDownloadCompleted = any(),
                    onDownloading = any(),
                ),
            ).thenAnswer {
                val onDownloadCompletedCallback = it.getArgument<(String) -> Unit>(1)
                onDownloadCompletedCallback.invoke(fakeUriPath)
            }
            whenever(versionRepository.getUrl()).thenReturn(fakeUriPath)
            val context: Context = mock()

            with(downloadNewVersion(context)) {
                assertTrue(isSuccess)
            }
        }

    @Test
    fun `should return failure if an exception is thrown`() =
        runTest {
            given(versionRepository.download(any(), any(), any())) willAnswer {
                throw DomainError.DatabaseError("Test")
            }
            given(versionRepository.getUrl()) willAnswer {
                throw DomainError.DatabaseError("Test")
            }
            val context: Context = mock()

            with(downloadNewVersion(context)) {
                assertTrue(isFailure)
            }
        }
}
