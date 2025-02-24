package org.dhis2.usescases.biometrics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLevel4DistrictParent {
    @Test
    fun `should return empty level less than 4`() {
        val result = getLevel4DistrictParent(3, "/1/2/3")

        assertTrue(result.isBlank())
    }

    @Test
    fun `should return expected level 4 if current is 5`() {
        val result = getLevel4DistrictParent(5, "/1/2/3/4/5")
        assertEquals("4", result)
    }

    @Test
    fun `should return expected level 4 if current is 6`() {
        val result = getLevel4DistrictParent(6, "/1/2/3/4/5/6")
        assertEquals("4", result)
    }

    @Test
    fun `should return empty with invalid path`() {
        val result = getLevel4DistrictParent(5, "/1/2")
        assertEquals("", result)
    }

    @Test
    fun `should return empty with empty path`() {
        val result = getLevel4DistrictParent(5, "")
        assertEquals("", result)
    }
}