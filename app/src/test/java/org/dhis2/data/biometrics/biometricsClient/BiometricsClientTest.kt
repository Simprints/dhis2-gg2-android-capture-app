package org.dhis2.data.biometrics.biometricsClient

import android.app.Activity
import android.content.Intent
import com.google.gson.Gson
import com.simprints.libsimprints.Constants
import org.dhis2.data.biometrics.biometricsClient.models.ConfirmIdentityResult
import org.dhis2.data.biometrics.biometricsClient.models.IdentifyResult
import org.dhis2.data.biometrics.biometricsClient.models.RegisterResult
import org.dhis2.data.biometrics.biometricsClient.models.VerifyResult
import org.dhis2.data.biometrics.biometricsClient.models.sid.IdentificationSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.RegistrationSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.ScannedCredentialSID
import org.dhis2.data.biometrics.biometricsClient.models.sid.VerificationSID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@RunWith(JUnit4::class)
class BiometricsClientTest {
    @Test
    fun `Should return failure when identify result code is not ok`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent()

        val result = client.handleIdentifyResponse(Activity.RESULT_CANCELED, data)

        assertEquals(IdentifyResult.Failure, result)
    }

    @Test
    fun `Should return age group not supported when identify result code is age group not supported`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent()

        val result = client.handleIdentifyResponse(Constants.SIMPRINTS_AGE_GROUP_NOT_SUPPORTED, data)

        assertEquals(IdentifyResult.AgeGroupNotSupported, result)
    }

    @Test
    fun `Should return failure when identify biometrics completed flag is false`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(biometricsCompleted = false)

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data)

        assertEquals(IdentifyResult.Failure, result)
    }

    @Test
    fun `Should return user not found when identify returns no identifications`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(biometricsCompleted = true, sessionId = "session1")

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data)

        assertEquals(IdentifyResult.UserNotFound("session1", null), result)
    }

    @Test
    fun `Should carry scanned credential when identify returns no identifications`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(
            biometricsCompleted = true,
            sessionId = "session1",
            scannedCredentialSID = givenAScannedCredentialSID(type = "NHIS", value = "credentialValue"),
        )

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data) as IdentifyResult.UserNotFound

        assertEquals("credentialValue", result.scannedCredential?.value)
    }

    @Test
    fun `Should return user not found when all identifications are below confidence score filter`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 50)
        val data = givenAnIntent(
            biometricsCompleted = true,
            sessionId = "session1",
            identifications = listOf(
                givenAnIdentificationSID(guid = "guid1", confidence = 10f),
            ),
        )

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data)

        assertEquals(IdentifyResult.UserNotFound("session1", null), result)
    }

    @Test
    fun `Should keep identifications at or above the confidence score filter`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 50)
        val data = givenAnIntent(
            biometricsCompleted = true,
            sessionId = "session1",
            identifications = listOf(
                givenAnIdentificationSID(guid = "guid1", confidence = 10f),
                givenAnIdentificationSID(guid = "guid2", confidence = 90f),
            ),
        )

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data) as IdentifyResult.Completed

        assertEquals(listOf("guid2"), result.items.map { it.guid })
    }

    @Test
    fun `Should keep credential linked identifications even below the confidence score filter`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 50)
        val data = givenAnIntent(
            biometricsCompleted = true,
            sessionId = "session1",
            identifications = listOf(
                givenAnIdentificationSID(guid = "guid1", confidence = 10f, isLinkedToCredential = true),
            ),
        )

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data) as IdentifyResult.Completed

        assertEquals(listOf("guid1"), result.items.map { it.guid })
    }

    @Test
    fun `Should return biometrics declined when a refusal form is present without identifications`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(biometricsCompleted = true, refusalFormPresent = true)

        val result = client.handleIdentifyResponse(Activity.RESULT_OK, data)

        assertEquals(IdentifyResult.BiometricsDeclined, result)
    }

    @Test
    fun `Should return completed with scanned credential when register returns a registration`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(
            biometricsCompleted = true,
            registration = givenARegistrationSID(guid = "guid1"),
            hasCredential = true,
            scannedCredentialSID = givenAScannedCredentialSID(type = "NHIS", value = "credentialValue"),
        )

        val result = client.handleRegisterResponse(Activity.RESULT_OK, data) as RegisterResult.Completed

        assertEquals("guid1", result.item.guid)
        assertTrue(result.item.hasCredential)
        assertEquals("credentialValue", result.item.scannedCredential?.value)
    }

    @Test
    fun `Should return register last failure when register result code is register last biometrics failed`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent()

        val result = client.handleRegisterResponse(
            Constants.SIMPRINTS_ENROLMENT_LAST_BIOMETRICS_FAILED,
            data,
        )

        assertEquals(RegisterResult.RegisterLastFailure, result)
    }

    @Test
    fun `Should return possible duplicates when register returns identifications`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(
            biometricsCompleted = true,
            sessionId = "session1",
            identifications = listOf(givenAnIdentificationSID(guid = "guid1", confidence = 100f)),
        )

        val result = client.handleRegisterResponse(Activity.RESULT_OK, data) as RegisterResult.PossibleDuplicates

        assertEquals(listOf("guid1"), result.items.map { it.guid })
        assertEquals("session1", result.sessionId)
    }

    @Test
    fun `Should return no match when verify result code is not ok`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent()

        val result = client.handleVerifyResponse(Activity.RESULT_CANCELED, data)

        assertEquals(VerifyResult.NoMatch, result)
    }

    @Test
    fun `Should return match when simprints reports verification success`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(biometricsCompleted = true, simprintsVerificationSuccess = true)

        val result = client.handleVerifyResponse(Activity.RESULT_OK, data)

        assertEquals(VerifyResult.Match, result)
    }

    @Test
    fun `Should return no match when simprints reports verification failure`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(biometricsCompleted = true, simprintsVerificationSuccess = false)

        val result = client.handleVerifyResponse(Activity.RESULT_OK, data)

        assertEquals(VerifyResult.NoMatch, result)
    }

    @Test
    fun `Should return match when dhis2 verification confidence is at or above the filter for a matched band`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 50)
        val data = givenAnIntent(
            biometricsCompleted = true,
            verification = givenAVerificationSID(confidence = 90f, confidenceBand = "HIGH"),
        )

        val result = client.handleVerifyResponse(Activity.RESULT_OK, data)

        assertEquals(VerifyResult.Match, result)
    }

    @Test
    fun `Should return no match when dhis2 verification confidence is below the filter for a matched band`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 50)
        val data = givenAnIntent(
            biometricsCompleted = true,
            verification = givenAVerificationSID(confidence = 10f, confidenceBand = "HIGH"),
        )

        val result = client.handleVerifyResponse(Activity.RESULT_OK, data)

        assertEquals(VerifyResult.NoMatch, result)
    }

    @Test
    fun `Should return no match when dhis2 verification confidence band is none`() {
        val client = givenABiometricsClient(confidenceScoreFilter = 0)
        val data = givenAnIntent(
            biometricsCompleted = true,
            verification = givenAVerificationSID(confidence = 100f, confidenceBand = "NONE"),
        )

        val result = client.handleVerifyResponse(Activity.RESULT_OK, data)

        assertEquals(VerifyResult.NoMatch, result)
    }

    @Test
    fun `Should return completed when confirm identity response has no credential`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(hasCredential = false)

        val result = client.handleConfirmIdentityResponse(Activity.RESULT_OK, data)

        assertEquals(ConfirmIdentityResult.Completed, result)
    }

    @Test
    fun `Should return completed with credentials when confirm identity response has a scanned credential`() {
        val client = givenABiometricsClient()
        val data = givenAnIntent(
            hasCredential = true,
            scannedCredentialSID = givenAScannedCredentialSID(type = "NHIS", value = "credentialValue"),
        )

        val result =
            client.handleConfirmIdentityResponse(Activity.RESULT_OK, data) as ConfirmIdentityResult.CompletedWithCredentials

        assertEquals("credentialValue", result.item.scannedCredential!!.value)
    }

    @Test
    fun `Should return completed when confirm identity response data is null`() {
        val client = givenABiometricsClient()

        val result = client.handleConfirmIdentityResponse(Activity.RESULT_OK, null)

        assertEquals(ConfirmIdentityResult.Completed, result)
    }

    private fun givenABiometricsClient(confidenceScoreFilter: Int = 0) =
        BiometricsClient(
            projectId = "projectId",
            user = "user",
            confidenceScoreFilter = confidenceScoreFilter,
            forkVersion = "forkVersion",
        )

    private fun givenAnIntent(
        biometricsCompleted: Boolean? = null,
        sessionId: String? = null,
        identifications: List<IdentificationSID>? = null,
        refusalFormPresent: Boolean = false,
        registration: RegistrationSID? = null,
        hasCredential: Boolean? = null,
        scannedCredentialSID: ScannedCredentialSID? = null,
        verification: VerificationSID? = null,
        simprintsVerificationSuccess: Boolean? = null,
    ): Intent {
        val data: Intent = mock()

        whenever(data.getBooleanExtra(Constants.SIMPRINTS_BIOMETRICS_COMPLETE_CHECK, false)) doReturn
            (biometricsCompleted ?: false)

        whenever(data.hasExtra(Constants.SIMPRINTS_IDENTIFICATIONS)) doReturn (identifications != null)
        whenever(data.hasExtra("enrolment")) doReturn (registration != null)

        whenever(data.getStringExtra(Constants.SIMPRINTS_SESSION_ID)) doReturn sessionId

        whenever(data.getStringExtra(Constants.SIMPRINTS_IDENTIFICATIONS)) doReturn
            identifications?.let { Gson().toJson(it) }

        if (refusalFormPresent) {
            whenever(data.getStringExtra(Constants.SIMPRINTS_REFUSAL_FORM)) doReturn
                Gson().toJson(mapOf("reason" to "reason", "extra" to "extra"))
        }

        whenever(data.getStringExtra("enrolment")) doReturn registration?.let { Gson().toJson(it) }

        whenever(data.getBooleanExtra("hasCredential", false)) doReturn (hasCredential ?: false)

        whenever(data.getStringExtra("scannedCredential")) doReturn
            scannedCredentialSID?.let { Gson().toJson(it) }

        whenever(data.getStringExtra(Constants.SIMPRINTS_VERIFICATION)) doReturn
            verification?.let { Gson().toJson(it) }

        if (simprintsVerificationSuccess != null) {
            val extras: android.os.Bundle = mock()
            whenever(extras.containsKey(Constants.SIMPRINTS_VERIFICATION_SUCCESS)) doReturn true
            whenever(data.extras) doReturn extras
            whenever(data.getBooleanExtra(Constants.SIMPRINTS_VERIFICATION_SUCCESS, false)) doReturn
                simprintsVerificationSuccess
        }

        return data
    }

    private fun givenAnIdentificationSID(
        guid: String,
        confidence: Float,
        isLinkedToCredential: Boolean = false,
    ): IdentificationSID =
        IdentificationSID(
            guid = guid,
            confidence = confidence,
            confidenceBand = "HIGH",
            isLinkedToCredential = isLinkedToCredential,
            isVerified = false,
        )

    private fun givenARegistrationSID(guid: String): RegistrationSID =
        RegistrationSID(guid = guid)

    private fun givenAScannedCredentialSID(type: String, value: String): ScannedCredentialSID =
        ScannedCredentialSID(type = type, value = value)

    private fun givenAVerificationSID(confidence: Float, confidenceBand: String): VerificationSID =
        VerificationSID(guid = "guid1", confidence = confidence, confidenceBand = confidenceBand)
}
