package org.dhis2.usescases.biometrics

import org.junit.Assert
import org.junit.Test

class OrgUnitAsModuleIdTest {

    private val orgUnit1 = "orgUnit1"
    private val orgUnit2 = "orgUnit2"
    private val orgUnit3 = "orgUnit3"
    private val orgUnit4 = "orgUnit4"
    private val orgUnit5 = "orgUnit5"
    private val orgUnit6 = "orgUnit6"

    private val pathOrgUnit1 = "/$orgUnit1"
    private val pathOrgUnit2 = "/$orgUnit1/$orgUnit2"
    private val pathOrgUnit3 = "/$orgUnit1/$orgUnit2/$orgUnit3"
    private val pathOrgUnit4 = "/$orgUnit1/$orgUnit2/$orgUnit3/$orgUnit4"
    private val pathOrgUnit5 = "/$orgUnit1/$orgUnit2/$orgUnit3/$orgUnit4/$orgUnit5"
    private val pathOrgUnit6 = "/$orgUnit1/$orgUnit2/$orgUnit3/$orgUnit4/$orgUnit5/$orgUnit6"

    @Test
    fun `Should return the same org unit if orgUnitLevelAsModuleId is 0 and org unit is of level 1`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit1, 1, pathOrgUnit1, 0)

        Assert.assertEquals(orgUnit1, orgUnit)
    }

    @Test
    fun `Should return the same org unit if orgUnitLevelAsModuleId is 0 and org unit is of level 2`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit2, 2, pathOrgUnit2, 0)

        Assert.assertEquals(orgUnit2, orgUnit)
    }

    @Test
    fun `Should return the same org unit if orgUnitLevelAsModuleId is 0 and org unit is of level 3`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit3, 3, pathOrgUnit3, 0)

        Assert.assertEquals(orgUnit3, orgUnit)
    }

    @Test
    fun `Should return the same org unit if orgUnitLevelAsModuleId is 0 and org unit is of level 4`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit4, 4, pathOrgUnit4, 0)

        Assert.assertEquals(orgUnit4, orgUnit)
    }

    @Test
    fun `Should return max org unit level 4 if orgUnitLevelAsModuleId is 0 and org unit is of level 5`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit5, 5, pathOrgUnit5, 0)

        Assert.assertEquals(orgUnit4, orgUnit)
    }

    @Test
    fun `Should return max org unit level 4 if orgUnitLevelAsModuleId is 0 and org unit is of level 6`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit6, 6, pathOrgUnit6, 0)

        Assert.assertEquals(orgUnit4, orgUnit)
    }


    @Test
    fun `Should return parent org unit if orgUnitLevelAsModuleId is -1 and org unit is of level 2`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit2, 2, pathOrgUnit2, -1)

        Assert.assertEquals(orgUnit1, orgUnit)
    }

    @Test
    fun `Should return parent org unit if orgUnitLevelAsModuleId is -1 and org unit is of level 3`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit3, 3, pathOrgUnit3, -1)

        Assert.assertEquals(orgUnit2, orgUnit)
    }

    @Test
    fun `Should return parent org unit if orgUnitLevelAsModuleId is -1 and org unit is of level 4`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit4, 4, pathOrgUnit4, -1)

        Assert.assertEquals(orgUnit3, orgUnit)
    }

    @Test
    fun `Should return parent org unit if orgUnitLevelAsModuleId is -1 and org unit is of level 5`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit5, 5, pathOrgUnit5, -1)

        Assert.assertEquals(orgUnit4, orgUnit)
    }

    @Test
    fun `Should return max org unit level 4 if orgUnitLevelAsModuleId is -1 and org unit is of level 6`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit6, 6, pathOrgUnit6, -1)

        Assert.assertEquals(orgUnit4, orgUnit)
    }

    @Test
    fun `Should return the parent org unit if orgUnitLevelAsModuleId is -1 and level is less than max`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit3, 3, pathOrgUnit3, -1)

        Assert.assertEquals(orgUnit2, orgUnit)
    }

    @Test
    fun `Should return the parent of the parent org unit if orgUnitLevelAsModuleId is -2 and level is less than max`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit3, 3, pathOrgUnit3, -2)

        Assert.assertEquals(orgUnit1, orgUnit)
    }

    @Test
    fun `Should return last parent if orgUnitLevelAsModuleId is greater than parent counts`() {
        val orgUnit = getOrgUnitAsModuleIdByPath(orgUnit3, 3, pathOrgUnit3, -3)

        Assert.assertEquals(orgUnit1, orgUnit)
    }

    @Test
    fun `Should return selected org unit if doesn't exist in path`() {
        val orgUnit = getOrgUnitAsModuleIdByPath("orgUnit9", 9, pathOrgUnit6, -3)

        Assert.assertEquals("orgUnit9", orgUnit)
    }
}