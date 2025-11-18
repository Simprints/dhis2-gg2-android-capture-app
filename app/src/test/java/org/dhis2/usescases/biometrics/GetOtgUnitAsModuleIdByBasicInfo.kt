package org.dhis2.usescases.biometrics

import org.dhis2.data.biometrics.biometricsClient.BiometricsClient
import org.junit.Assert.assertEquals
import org.junit.Test

class GetOtgUnitAsModuleIdByBasicInfo {
    @Test
    fun `should return defaultModuleId if there are different level 4 parents`() {
        val result = getOrgUnitAsModuleIdByBasicInfo(
            listOf(
                BasicInfoOrgUnit("6", 6, "/1/2/3/4a/5/6"),
                BasicInfoOrgUnit("5", 5, "/1/2/3/4b/5/6")
            )
        )

        assertEquals(BiometricsClient.DefaultModuleId, result)
    }

    @Test
    fun `should return the unique level 4 parent`() {
        val result = getOrgUnitAsModuleIdByBasicInfo(
            listOf(
                BasicInfoOrgUnit("6", 6, "/1/2/3/4/5/6"),
                BasicInfoOrgUnit("5", 5, "/1/2/3/4/5")
            )
        )

        assertEquals("4", result)
    }
}