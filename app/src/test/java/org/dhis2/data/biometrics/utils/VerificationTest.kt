package org.dhis2.data.biometrics.utils

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class VerificationTest {
    @Test
    fun `Should return verifications only with new one if old verifications is empty`() {
        val oldVerifications = listOf<BiometricsVerification>()

        val newVerifications =
            calculateNewVerifications(
                "uid1",
                oldVerifications,
                lastVerificationDuration = 2
            )

        assertTrue(newVerifications.size == 1)
        assertTrue(newVerifications[0].teiUid == "uid1")
    }

    @Test
    fun `Should return verifications only with new one if old verifications are inactive`() {
        val oldVerifications =
            listOf(BiometricsVerification("uid1", givenADatePreviousFromNow(5).time),
                BiometricsVerification("uid2", givenADatePreviousFromNow(3).time))

        val newVerifications =
            calculateNewVerifications(
                "uid1",
                oldVerifications,
                lastVerificationDuration = 2
            )

        assertTrue(newVerifications.size == 1)
        assertTrue(newVerifications[0].teiUid == "uid1")
    }

    @Test
    fun `Should return verifications with old and new one if old verifications are active`() {
        val oldVerifications =
            listOf(BiometricsVerification("uid1", givenADatePreviousFromNow(1).time),
                BiometricsVerification("uid2", givenADatePreviousFromNow(0).time))

        val newVerifications =
            calculateNewVerifications(
                "uid3",
                oldVerifications,
                lastVerificationDuration = 2
            )

        assertTrue(newVerifications.size == 3)
        assertTrue(newVerifications[2].teiUid == "uid3")
    }

    @Test
    fun `Should return verifications with old different of new and new one if old verifications are active`() {
        val oldVerifications =
            listOf(BiometricsVerification("uid1", givenADatePreviousFromNow(1).time),
                BiometricsVerification("uid2", givenADatePreviousFromNow(0).time))

        val newVerifications =
            calculateNewVerifications(
                "uid2",
                oldVerifications,
                lastVerificationDuration = 2
            )

        assertTrue(newVerifications.size == 2)
        assertTrue(newVerifications[1].teiUid == "uid2")
    }

    private fun givenADatePreviousFromNow(minutes: Int): Date {
        val timeInSecs: Long = Date().time
        return Date(timeInSecs - minutes * 60 * 1000)
    }
}
